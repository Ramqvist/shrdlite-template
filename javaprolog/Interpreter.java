import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.AtomicTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;

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
		convertFromJSON(world, objects);
	}
	
	/** Converts the given JSON input to a two-dimensional list of {@link Entity} objects.*/
	private void convertFromJSON(JSONArray world, JSONObject objects) {
		for (int i = 0; i < world.size(); i++) {
			JSONArray stack = (JSONArray) world.get(i);
			
			ArrayList<Entity> column = new ArrayList<Entity>();
			
			for (int j = 0; j < stack.size(); j++) {
				String name = (String) stack.get(j);
				JSONObject entityDescription = (JSONObject) objects.get(name);
				
				String form = (String) entityDescription.get("form");
				Entity.FORM eForm = Entity.FORM.BRICK;
				switch(form) {
				case "brick":
					eForm = Entity.FORM.BRICK;
					break;
				case "plank":
					eForm = Entity.FORM.PLANK;
					break;
				case "ball":
					eForm = Entity.FORM.BALL;
					break;
				case "table":
					eForm = Entity.FORM.TABLE;
					break;
				case "pyramid":
					eForm = Entity.FORM.PYRAMID;
					break;
				case "box":
					eForm = Entity.FORM.BOX;
					break;
				}
				
				String size = (String) entityDescription.get("size");
				Entity.SIZE eSize = Entity.SIZE.LARGE;
				switch(size) {
				case "large":
					eSize = Entity.SIZE.LARGE;
					break;
				case "small":
					eSize = Entity.SIZE.SMALL;
					break;
				}
				
				String color = (String) entityDescription.get("color");
				Entity.COLOR eColor = Entity.COLOR.GREEN;
				switch(color) {
				case "green":
					eColor = Entity.COLOR.GREEN;
					break;
				case "white":
					eColor = Entity.COLOR.WHITE;
					break;
				case "red":
					eColor = Entity.COLOR.RED;
					break;
				case "black":
					eColor = Entity.COLOR.BLACK;
					break;
				case "blue":
					eColor = Entity.COLOR.BLUE;
					break;
				case "yellow":
					eColor = Entity.COLOR.YELLOW;
					break;
				}
				
				Entity newEntity = new Entity(eForm, eSize, eColor);
				column.add(newEntity);
			}
			
			this.world.add(column);
		}
		System.out.println(this.world);
		System.out.println();
	}
	
	public List<Goal> interpret(Term tree) {
//		System.out.println("DEREFERENCE");
//		System.out.println(Arrays.asList(cterm.args));
//		System.out.println();
		
		walkTree(tree);
		
		List<Goal> goalList = new ArrayList<Goal>();
		return goalList;
	}

	public void walkTree(Term term) {
		if (term instanceof CompoundTerm) {
			CompoundTerm cterm = (CompoundTerm) term;
			switch(cterm.tag.functor.toString()) {
			case "move":
				System.out.println("saw move");
				walkTree(cterm.args[0]);
				walkTree(cterm.args[1]);
				break;
			case "relative":
				System.out.println("saw relative");
				walkTree(cterm.args[0]);
				walkTree(cterm.args[1]);
				break;
			case "basic_entity":
				System.out.println("saw basic_entity");
				walkTree(cterm.args[0]);
				walkTree(cterm.args[1]);
				break;
			case "relative_entity":
				System.out.println("saw relative_entity");
				walkTree(cterm.args[0]);
				walkTree(cterm.args[1]);
				walkTree(cterm.args[2]);
				break;
			case "object":
				System.out.println("saw object");
				walkTree(cterm.args[0]);
				walkTree(cterm.args[1]);
				walkTree(cterm.args[2]);
				break;
			}
//		} else if (term instanceof AtomTerm) {
//			AtomTerm aterm = (AtomTerm) term;
//			switch(aterm.value) {
//			case "floor":
//				System.out.println("FOUND FLOOR");
//				break;
//			case "the":
//			case "any":
//			case "all":
//				break;
//			case "beside":
//			case "leftof":
//			case "rightof":
//			case "above":
//			case "ontop":
//			case "under":
//			case "inside":
//				break;
//			case "small":
//			case "large":
//			}
		} else if (term instanceof VariableTerm) {
			
		}
		System.out.println();
	}
	
}
