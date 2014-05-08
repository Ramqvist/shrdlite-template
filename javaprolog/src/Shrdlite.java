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
import src.world.Entity;
import src.world.Goal;
import src.world.Relation;

public class Shrdlite {
	
	public static PlannerAlgorithm algorithm = PlannerAlgorithm.STOCHASTIC;
	
	private static JSONArray utterance;
	private static JSONArray world;
	private static String holding;
	private static JSONObject objects;
	private static JSONObject state;
	private static JSONArray statearray;
	private static JSONObject result;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws PrologException, ParseException, IOException {
		JSONObject jsinput = (JSONObject) JSONValue.parse(readFromStdin());
		utterance = (JSONArray) jsinput.get("utterance");
		world = (JSONArray) jsinput.get("world");
		holding = (String) jsinput.get("holding");
		objects = (JSONObject) jsinput.get("objects");
		state = (JSONObject) jsinput.get("state");
		statearray = new JSONArray();
		
		result = new JSONObject();
		result.put("utterance", utterance);
		
		parseAndPlan();

		System.out.print(result);
	}
	
	private static List<Goal> goals = new ArrayList<>();
	
	private static void parseAndPlan() throws PrologException, IOException {
		Interpreter interpreter = parse();
		if (interpreter != null) {
			plan(interpreter.world, interpreter.heldEntity);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Interpreter parse() throws PrologException, IOException {
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
			return null;
		} else if (trees.size() > 1000) {
			// TODO: Decide if we want to keep this.
			result.put("output", "That sentence is ambiguous. Please specify with more clarity what you wish me to do.");
			return null;
		}
		
		Interpreter interpreter = new Interpreter(world, holding, objects);
		
		for (Term tree : trees) {
			if (interpreter.checkForCancel(tree)) {
				result.put("output", "Okay, I will forget the last things you've said.");
				result.put("state", new JSONObject());
				return null;
			}
			for (Goal goal : interpreter.interpret(tree)) {
				goals.add(goal);
			}
		}
		
		tryToResolveAmbiguity(parser);

		JSONArray goalArray = new JSONArray();
		for (Goal g : goals) {
			goalArray.add(g.toString());
		}
		result.put("goals", goalArray);
		
		if (goals.isEmpty()) {
			if (result.get("output") == null) {
				result.put("output", "Interpretation error!");
			}
			return null;
		} else if (goals.size() > 1000) {
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
			return null;
		}
		state.put("utterances", new JSONArray());
		result.put("state", state);
		return interpreter;
	}

	private static void tryToResolveAmbiguity(DCGParser parser) throws PrologException, IOException {
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
									Debug.print("Kept goal: " + stateGoal);
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
	}

	@SuppressWarnings("unchecked")
	private static void plan(List<List<Entity>> world, Entity heldEntity) {
		IGoalSolver goalSolver;
		List<? extends IPlan> plans;
		if (algorithm == PlannerAlgorithm.HEURISTIC) {
			goalSolver = new HeuristicGoalSolver(world, heldEntity, goals);
		} else if (algorithm == PlannerAlgorithm.PROBABILITY) {
			goalSolver = new ProbabilisticSolver(world, heldEntity, goals);
		} else if (algorithm == PlannerAlgorithm.LIMITED_HEURISTIC) {
			goalSolver = new LimitedHeuristicSolver(world, heldEntity, goals);
		} else if (algorithm == PlannerAlgorithm.STOCHASTIC) {
			goalSolver = new StochasticSolver(world, heldEntity, goals);
		} else {
			goalSolver = new BreadthFirstSolver(world, heldEntity, goals);
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
	
	public static String readFromStdin() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder data = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			data.append(line).append('\n');
		}
		return data.toString();
	}
	
	public static int getMeanValue(List<Long> testList) {
		int sum = 0;
		for(long i : testList) 
			sum += i;
			
		return sum / testList.size();
	}
	
	public static void runBenchmark(List<List<Entity>> world, Entity heldEntity, List<Goal> goals, int runs) {
		
		Debug.print("==========================================================");
		Debug.print("======== ADVANCED PLANNER ALGORITHMS BENCHMARK STARTED ");
		Debug.print("==========================================================");
		Debug.print();
		List<? extends IPlan> plans;
		IGoalSolver goalSolver;
		if (algorithm == PlannerAlgorithm.HEURISTIC) {
			goalSolver = new HeuristicGoalSolver(world, heldEntity, goals);
		} else if (algorithm == PlannerAlgorithm.PROBABILITY) {
			goalSolver = new ProbabilisticSolver(world, heldEntity, goals);
		} else if (algorithm == PlannerAlgorithm.LIMITED_HEURISTIC) {
			goalSolver = new LimitedHeuristicSolver(world, heldEntity, goals);
		} else if (algorithm == PlannerAlgorithm.STOCHASTIC) {
			goalSolver = new StochasticSolver(world, heldEntity, goals);
		} else {
			goalSolver = new BreadthFirstSolver(world, heldEntity, goals);
		}
		List<Long> times = new ArrayList<>();
		for(int i = 0 ; i < runs ; i++ ) {
			long start = System.currentTimeMillis();
			plans = goalSolver.solve();
			long elapsed = System.currentTimeMillis() - start;
			if(plans == null || plans.isEmpty()) Debug.print("Plans was NULL! Call the 911!");
			times.add(elapsed);
			Debug.print("RUN " + String.valueOf(i+1) + " finished : " + elapsed + " ms");
		}
		Debug.print();
		Debug.print("==========================================================");
		Debug.print("======== ADVANCED PLANNER ALGORITHMS BENCHMARK FINISHED ");
		Debug.print("==========================================================");
		Debug.print();
		for(int i = 0 ; i < times.size() ; i++) {
			Debug.print("RUN " + String.valueOf(i+1) + " : " + times.get(i) + " ms");
		}
		Debug.print();
		Debug.print("MEAN VALUE " + getMeanValue(times) + " ms");
		
	}
	
	

}
