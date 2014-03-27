import gnu.prolog.term.AtomTerm;
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
				Entity newEntity = new Entity((String) entityDescription.get("form"), (String) entityDescription.get("size"), (String) entityDescription.get("color"));
				column.add(newEntity);
			}
			
			this.world.add(column);
		}
		System.out.println("World Representation");		
		System.out.println(this.world);
		System.out.println();
	}
	
	public List<Goal> interpret(Term tree) {
		System.out.println("=================");
		System.out.println("START OF INTERPRET");
		System.out.println();
		List<Relation> relations = new ArrayList<>();
		walkTree(tree, relations);
		System.out.println(relations);
		System.out.println();
		System.out.println("END OF INTERPRET");
		System.out.println("================");
		System.out.println();
		latestEntity = null;
		latestRelation = null;
		List<Goal> goalList = new ArrayList<Goal>();
		return goalList;
	}

	private Entity latestEntity;
	private Relation.TYPE latestRelation;
	
	public void walkTree(Term term, List<Relation> relations) {
		if (term instanceof CompoundTerm) {
			CompoundTerm cterm = (CompoundTerm) term;
			switch(cterm.tag.functor.toString()) {
			case "move":
				System.out.println("saw move");
				walkTree(cterm.args[0], relations);
				walkTree(cterm.args[1], relations);
				break;
			case "relative":
				System.out.println("saw relative");
				latestRelation = Relation.parseType(cterm.args[0].toString());
				walkTree(cterm.args[1], relations);
				break;
			case "basic_entity":
				System.out.println("saw basic_entity");
				walkTree(cterm.args[0], relations);
				walkTree(cterm.args[1], relations);
				break;
			case "relative_entity":
				System.out.println("saw relative_entity");
				walkTree(cterm.args[0], relations);
				walkTree(cterm.args[1], relations);
				walkTree(cterm.args[2], relations);
				break;
			case "object":
				System.out.println("saw object");
				Entity entity = new Entity(cterm.args[0].toString(), cterm.args[1].toString(), cterm.args[2].toString());
				if (latestEntity != null) {
					Relation relation = new Relation(latestEntity, entity, latestRelation);
					relations.add(relation);
					latestRelation = null;
				}
				latestEntity = entity;
				System.out.println("latest entity changed to " + latestEntity);
				// compare to world
				List<Entity> matchedEntities = new ArrayList<>();
				System.out.println(entity);
				for (List<Entity> column : world) {
					System.out.println(column);
					if (column.contains(entity)) {
						matchedEntities.add(entity);
					}
				}
				
//				// check ambiguity
//				if (matchedEntities.size() > 1) {
//					System.out.println("Ambiguity, these objects match this entity " + entity);
//					System.out.println(matchedEntities);
//				}
				
				if (matchedEntities.isEmpty()) {
					// TODO get real man
					System.out.println("Error: [" + entity + "] does not exists in the world.");
				} else {
					System.out.println("Success: [" + entity + "] exists in the world.");
				}
				break;
			}
		} else if (term instanceof AtomTerm) {
			AtomTerm aterm = (AtomTerm) term;
			switch(aterm.value) {
			case "floor":
				System.out.println("saw floor");
				latestEntity = new Entity(Entity.FORM.FLOOR, Entity.SIZE.UNDEFINED, Entity.COLOR.UNDEFINED);
				System.out.println("latest entity changed to " + latestEntity);
				break;
			}
		}
		System.out.println();
	}
	
}
