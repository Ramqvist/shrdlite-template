import gnu.prolog.term.Term;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *  The interpreter class
 */
public class Interpreter {
	
	List<List<Entity>> world = new ArrayList<List<Entity>>();

	public Interpreter(JSONArray world, String holding, JSONObject objects) {
		//Parse to internal representation of the world?
	}
	
	public List<Goal> interpret(Term tree) {
		String s = tree.toString();
		//Split words to tree?
		//Parse to Goal
		List<Goal> goalList = new ArrayList<Goal>();
		return goalList;
	}

	
	
}
