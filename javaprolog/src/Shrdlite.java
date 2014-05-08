package src;
/** 
 * 
First compile the program:
javac -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. src.Shrdlite.java

- MEDIUM:
java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. src.Shrdlite < ../examples/medium.json

- COMPLEX:
java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. src.Shrdlite < ../examples/complex.json

- SMALL:
java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. src.Shrdlite < ../examples/small.json

Run Server from Console
python -m CGIHTTPServer 8000
 
 */
import gnu.prolog.term.Term;
import gnu.prolog.vm.PrologException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import src.interpreter.Interpreter;
import src.planner.BreadthFirstSolver;
import src.planner.HeuristicGoalSolver;
import src.planner.IGoalSolver;
import src.planner.IGoalSolver.PlannerAlgorithm;
import src.planner.LimitedHeuristicSolver;
import src.planner.ProbabilisticSolver;
import src.planner.StochasticSolver;
import src.planner.data.Action;
import src.planner.data.IPlan;
import src.world.Goal;
import src.world.Relation;

public class Shrdlite {
	
	public static PlannerAlgorithm algorithm = PlannerAlgorithm.HEURISTIC;

	public static void main(String[] args) throws PrologException, ParseException, IOException {
		JSONObject jsinput = (JSONObject) JSONValue.parse(readFromStdin());
		JSONArray utterance = (JSONArray) jsinput.get("utterance");
		JSONArray world = (JSONArray) jsinput.get("world");
		String holding = (String) jsinput.get("holding");
		JSONObject objects = (JSONObject) jsinput.get("objects");
		JSONObject state = (JSONObject) jsinput.get("state");
		JSONArray statearray = new JSONArray();
		
		JSONObject result = new JSONObject();
		result.put("utterance", utterance);
		

		DCGParser parser = new DCGParser("shrdlite_grammar.pl");
		
		//************
		RelationParser repa = new RelationParser("relations.pl");
		repa.checkRelations("above_test","X","e");
		//************

		List<Term> trees = parser.parseSentence("command", utterance);
		List<String> tstrs = new ArrayList<String>();
		result.put("trees", tstrs);
		Debug.print();
		for (Term t : trees) {
			tstrs.add(t.toString());
			Debug.print("Tree " + (trees.indexOf(t) + 1));
			Debug.print(t.toString());
			Debug.print();
		}

		if (trees.isEmpty()) {
			result.put("output", "Parse error!");
		} else {
			List<Goal> goals = new ArrayList<>();
			Interpreter interpreter = new Interpreter(world, holding, objects);
			if (trees.size() > 1) {
				result.put("output", "That sentence is ambiguous. Please specify with more clarity what you wish me to do.");
			} else {
				for (Term tree : trees) {
					for (Goal goal : interpreter.interpret(tree)) {
						goals.add(goal);
					}
				}
			
				// TODO: Use state to handle ambiguity resolution.
				if (state != null) {
					statearray = (JSONArray) state.get("utterances");
					if (statearray != null && !statearray.isEmpty()) {
						Debug.print("State given: " + state.toString());
						Debug.print();
						
						List<Goal> previousGoals = new ArrayList<>();
						
						for (Object object : statearray) {
							JSONArray stateUtterance = (JSONArray) object;
							
							List<Term> statetrees = parser.parseSentence("command", stateUtterance);
							List<String> statetstrs = new ArrayList<String>();
							for (Term t : statetrees) {
								statetstrs.add(t.toString());
								Debug.print("State Tree " + (statetrees.indexOf(t) + 1));
								Debug.print(t.toString());
								Debug.print();
							}
		
							Interpreter stateInterpreter = new Interpreter(world, holding, objects);
							List<Goal> newGoals = new ArrayList<>();
							for (Term tree : statetrees) {
								if (previousGoals.isEmpty()) {
									for (Goal stateGoal : stateInterpreter.interpret(tree)) {
										previousGoals.add(stateGoal);
									}
								} else {
									for (Goal stateGoal : stateInterpreter.interpret(tree)) {
										for (Goal goal : previousGoals) {
											for (Relation stateGoalRelation : stateGoal.getRelations()) {
												for (Relation goalRelation : goal.getRelations()) {
													if (stateGoalRelation.getEntityA().equals(goalRelation.getEntityA())
															|| stateGoalRelation.getEntityA().equals(goalRelation.getEntityB())) {
														newGoals.add(goal);
														Debug.print("Added goal: " + goal);
													}
												}
											}
										}
									}
									Debug.print(newGoals);
									previousGoals = newGoals;
								}
							}
						}
						List<Goal> newGoals = new ArrayList<>();
						for (Goal goal : goals) {
							for (Goal stateGoal : previousGoals) {
								for (Relation stateGoalRelation : stateGoal.getRelations()) {
									for (Relation goalRelation : goal.getRelations()) {
										if (stateGoalRelation.getEntityA().equals(goalRelation.getEntityA())
												|| stateGoalRelation.getEntityB().equals(goalRelation.getEntityA())) {
											newGoals.add(stateGoal);
											Debug.print("Later Added goal: " + stateGoal);
										}
									}
								}
							}
						}
						goals = newGoals;
					}
				} else {
					Debug.print("State was empty.");
					Debug.print();
				}
	
				JSONArray goalArray = new JSONArray();
				for (Goal g : goals) {
					goalArray.add(g.toString());
				}
				result.put("goals", goalArray);
			}
			if (goals.isEmpty()) {
				if (result.get("output") == null) {
					result.put("output", "Interpretation error!");
				}
			} else if (goals.size() > 1) {
				Debug.print("Ambiguity error!");
				for (Goal goal : goals) {
					Debug.print(goal);
				}
				Debug.print();
				if (statearray == null) {
					statearray = new JSONArray();
				}
				statearray.add(utterance);
				if (state == null) {
					state = new JSONObject();
				}
				state.put("utterances", statearray);
				result.put("state", state);
				result.put("output", "Ambiguity error!");
			} else {
				result.put("state", statearray);
				IGoalSolver goalSolver;
				List<? extends IPlan> plans;
				if (algorithm == PlannerAlgorithm.HEURISTIC) {
					goalSolver = new HeuristicGoalSolver(interpreter.world, interpreter.heldEntity, goals);
				} else if (algorithm == PlannerAlgorithm.PROBABILITY) {
					goalSolver = new ProbabilisticSolver(interpreter.world, interpreter.heldEntity, goals);
				} else if (algorithm == PlannerAlgorithm.LIMITED_HEURISTIC) {
					goalSolver = new LimitedHeuristicSolver(interpreter.world, interpreter.heldEntity, goals);
				} else if (algorithm == PlannerAlgorithm.STOCHASTIC) {
					goalSolver = new StochasticSolver(interpreter.world, interpreter.heldEntity, goals);
				} else {
					goalSolver = new BreadthFirstSolver(interpreter.world, interpreter.heldEntity, goals);
				}
				plans = goalSolver.solve();

				List<String> actionStrings = new ArrayList<>();
				if (!plans.isEmpty()) {
					List<Action> smallestPlan = plans.get(0).getActions();
					for(IPlan p : plans) {
						if(p.getActions().size() < smallestPlan.size()) {
							smallestPlan = p.getActions();
						}
					}

					Debug.print();
					Debug.print("Picked " + smallestPlan + "!");
					Debug.print();
					
					for (Action action: smallestPlan) {
						actionStrings.add(action.toString());
					}
					
					result.put("plan", actionStrings);
				}

				if (plans.isEmpty()) {
					result.put("output", "Planning error!");
				} else {
					result.put("output", "Success!");
					result.put("state", new JSONObject());
				}
			}
		}

		System.out.print(result);
	}

	public static String readFromStdin() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder data = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			data.append(line).append('\n');
		}
		return data.toString();
	}

}
