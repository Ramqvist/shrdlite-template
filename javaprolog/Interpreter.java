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
	
	List<Relation> relations; 
	
	public List<Goal> interpret(Term tree) {
		relations = new ArrayList<>();
		System.out.println("=================");
		System.out.println("START OF INTERPRET");
		System.out.println();
		try {
			walkTree(tree);
		} catch (InterpretationException e) {
			e.printStackTrace();
		}
		System.out.println();
		System.out.println("END OF INTERPRET");
		System.out.println("================");
		System.out.println();
		List<Goal> goalList = new ArrayList<Goal>();		
		goalList.add(new Goal(relations));
		// TODO handle error somehow
		System.out.println(goalList.get(0));
		return goalList;
	}
	
	private Entity undefinedEntity = new Entity(Entity.FORM.UNDEFINED, Entity.SIZE.UNDEFINED, Entity.COLOR.UNDEFINED);
	
	public class InterpretationException extends Exception {
		
		public InterpretationException(String message) {
			super(message);
		}
		
	}
	
	public Object walkTree(Term term) throws InterpretationException {
		Relation relation, finalRelation;
		Entity entity;
		
		if (term instanceof CompoundTerm) {
			CompoundTerm cterm = (CompoundTerm) term;
			switch(cterm.tag.functor.toString()) {
			case "move":
				System.out.println("saw move");
				entity = (Entity) walkTree(cterm.args[0]); // EITHER FLOOR, BASIC_ENTITY OR RELATIVE_ENTITY
				relation = (Relation) walkTree(cterm.args[1]); // ALWAYS RELATIVE
				finalRelation = new Relation(entity, relation.getEntityB(), relation.getType());
				relations.add(finalRelation);
				System.out.println("MOVE Added new relation to relations: " + finalRelation);
				return finalRelation;
			case "relative":
				System.out.println("saw relative");
				Relation.TYPE relationType = (Relation.TYPE) walkTree(cterm.args[0]); // ALWAYS RELATION
				entity = (Entity) walkTree(cterm.args[1]); // EITHER FLOOR, BASIC_ENTITY OR RELATIVE_ENTITY
				System.out.println("in relative, got relationtype: " + relationType);
				System.out.println("in relative, got entity: " + entity);
				relation = new Relation(undefinedEntity, entity, relationType);
				return relation;
			case "basic_entity":
				System.out.println("saw basic_entity");
				walkTree(cterm.args[0]); // ALWAYS QUANTIFIER
				entity = (Entity) walkTree(cterm.args[1]); // ALWAYS OBJECT (our class is called Entity)
				return entity;
			case "relative_entity":
				System.out.println("saw relative_entity");
				walkTree(cterm.args[0]); // ALWAYS QUANTIFIER
				entity = (Entity) walkTree(cterm.args[1]); // ALWAYS OBJECT (our class is called Entity)
				relation = (Relation) walkTree(cterm.args[2]); // ALWAYS RELATIVE
				Relation newRelation = new Relation(entity, relation.getEntityB(), relation.getType());
				relations.add(newRelation);
				System.out.println("RELATIVE_ENTITY Added new relation to relations: " + newRelation);
				return entity;
			case "object":
				System.out.println("saw object");
				entity = new Entity(cterm.args[0].toString(), cterm.args[1].toString(), cterm.args[2].toString());

				List<Entity> matchedEntities = new ArrayList<>();
				System.out.println(entity);
				for (List<Entity> column : world) {
					System.out.println(column);
					if (column.contains(entity)) {
						matchedEntities.add(column.get(column.indexOf(entity)));
					}
				}
				
//				// check ambiguity
//				if (matchedEntities.size() > 1) {
//					System.out.println("Ambiguity, these objects match this entity " + entity);
//					System.out.println(matchedEntities);
//				}
				
				if (matchedEntities.isEmpty()) {
					throw new InterpretationException("Error: [" + entity + "] does not exists in the world.");
				}
				System.out.println("Success: [" + entity + "] exists in the world as [" + matchedEntities.get(0) + "].");
				return matchedEntities.get(0);
			}
		} else if (term instanceof AtomTerm) {
			AtomTerm aterm = (AtomTerm) term;
			switch(aterm.value) {
			case "floor":
				System.out.println("saw floor");
				return new Entity(Entity.FORM.FLOOR, Entity.SIZE.UNDEFINED, Entity.COLOR.UNDEFINED);				
			}
			// This static method handles the parsing of type values.
			return Relation.parseType(aterm.value);
		}
		System.out.println();
		return null;
	}
	
}
