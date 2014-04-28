package src;
// First compile the program:
// javac -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. Shrdlite.java

// Then test from the command line:
// java -cp gnuprologjava-0.2.6.jar:json-simple-1.1.1.jar:. Shrdlite < ../examples/medium.json

import java.util.List;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import gnu.prolog.term.Term;
import gnu.prolog.vm.PrologException;

import org.json.simple.parser.ParseException;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import src.interpreter.InterpreterNew;
import src.planner.Action;
import src.planner.ConcurrentGoalSolver;
import src.planner.ErikTheSolver;
import src.planner.GoalSolver;
import src.planner.Plan;
import src.world.Goal;

public class Shrdlite {

	public static void main(String[] args) throws PrologException, ParseException, IOException {
		JSONObject jsinput = (JSONObject) JSONValue.parse(readFromStdin());
		JSONArray utterance = (JSONArray) jsinput.get("utterance");
		JSONArray world = (JSONArray) jsinput.get("world");
		String holding = (String) jsinput.get("holding");
		JSONObject objects = (JSONObject) jsinput.get("objects");

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
			InterpreterNew interpreter = new InterpreterNew(world, holding, objects);
			for (Term tree : trees) {
				for (Goal goal : interpreter.interpret(tree)) {
					goals.add(goal);
				}
			}

			JSONArray goalArray = new JSONArray();
			for (Goal g : goals) {
				goalArray.add(g.toString());
			}
			result.put("goals", goalArray);

			if (goals.isEmpty()) {
				result.put("output", "Interpretation error!");
			} else if (goals.size() > 1000) { // TODO: Temporarily changed so we can ignore ambiguity errors for now.
				result.put("output", "Ambiguity error!");
			} else {
				GoalSolver goalSolver;
				List<Plan> plans;
				if (true) {
					goalSolver = new ConcurrentGoalSolver(interpreter.world, interpreter.heldEntity, goals);
					plans = goalSolver.solve();
				} else if(true){
					goalSolver = new ErikTheSolver(interpreter.world, interpreter.heldEntity, goals);
					plans = goalSolver.solve();
				} else {
					goalSolver = new StandardGoalSolver(interpreter.world, interpreter.heldEntity, goals);
					plans = goalSolver.solve();
				}

				List<String> actionStrings = new ArrayList<>();
				if (!plans.isEmpty()) {
					List<Action> smallestPlan = plans.get(0).actions;
					for(Plan p : plans) {
						if(p.actions.size() < smallestPlan.size()) {
							smallestPlan = p.actions;
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
