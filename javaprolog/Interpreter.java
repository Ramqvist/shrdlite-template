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
		String s = tree.toString();
		//Split words to tree?
		//Parse to Goal
		List<Goal> goalList = new ArrayList<Goal>();
		return goalList;
	}

	
	
}
