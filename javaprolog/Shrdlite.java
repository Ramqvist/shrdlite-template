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
		List<Term> trees = parser.parseSentence("command", utterance);
		List tstrs = new ArrayList();
		result.put("trees", tstrs);
//		System.out.println();
		for (Term t : trees) {
			tstrs.add(t.toString());
			// DEBUG OUTPUT
//			System.out.println("Tree " + (trees.indexOf(t) + 1));
//			System.out.println(t.toString());
//			System.out.println();
		}

		if (trees.isEmpty()) {
			result.put("output", "Parse error!");

		} else {
			List<Goal> goals = new ArrayList<>();
			Interpreter interpreter = new Interpreter(world, holding, objects);
			for (Term tree : trees) {
				for (Goal goal : interpreter.interpret(tree)) {
					goals.add(goal);
				}
			}
			result.put("goals", "");

			if (goals.isEmpty()) {
				result.put("output", "Interpretation error!");

			} else if (goals.size() > 100) {
				result.put("output", "Ambiguity error!");
			} else {
				Planner planner = new Planner(interpreter.world);
				List<Plan> plans = new ArrayList<Plan>();
				for (Goal g : goals) {
					plans.add(planner.solve(g));
				}
				for (Plan p : plans) {
					List<String> actionStrings = new ArrayList<>();
					for (Action action : p.actions) {
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
