import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * The interpreter class
 */
public class Interpreter {

	List<List<Entity>> world = new ArrayList<List<Entity>>();

	public Interpreter(JSONArray world, String holding, JSONObject objects) {
		convertFromJSON(world, objects);
	}

	/**
	 * Converts the given JSON input to a two-dimensional list of {@link Entity}
	 * objects.
	 */
	private void convertFromJSON(JSONArray world, JSONObject objects) {
		for (int i = 0; i < world.size(); i++) {
			JSONArray stack = (JSONArray) world.get(i);

			ArrayList<Entity> column = new ArrayList<Entity>();

			for (int j = 0; j < stack.size(); j++) {
				String name = (String) stack.get(j);
				JSONObject entityDescription = (JSONObject) objects.get(name);
				Entity newEntity = new Entity((String) entityDescription.get("form"), (String) entityDescription.get("size"),
						(String) entityDescription.get("color"));
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
		List<Goal> goalList = new ArrayList<Goal>();
		relations = new ArrayList<>();

		System.out.println("=================");
		System.out.println("START OF INTERPRET");
		System.out.println();

		try {
			walkTree(tree);
			goalList.add(new Goal(relations));
		} catch (InterpretationException e) {
			System.out.println(e);
		}

		System.out.println();
		System.out.println("END OF INTERPRET");
		System.out.println("================");
		System.out.println();

		if (goalList.size() > 0)
			System.out.println(goalList.get(0));
		else
			System.out.println("No goal could be produced.");

		System.out.println();

		return goalList;
	}

	public class InterpretationException extends Exception {

		private static final long serialVersionUID = 2280978916235342656L;

		public InterpretationException(String message) {
			super(message);
		}

	}

	private Relation givenRelation;

	public Object walkTree(Term term) throws InterpretationException {
		Relation relation, finalRelation;
		Entity entity;

		if (term instanceof CompoundTerm) {
			CompoundTerm cterm = (CompoundTerm) term;
			switch (cterm.tag.functor.toString()) {
			case "move":
				/*
				 * Move has two children. The left child is always either floor,
				 * basic_entity or relative_entity. For us, this means it is
				 * always an Entity.
				 * 
				 * The right child is always relative. For us, this means it is
				 * always a Relation.
				 */
				System.out.println("saw move");
				entity = (Entity) walkTree(cterm.args[0]);
				relation = (Relation) walkTree(cterm.args[1]);
				finalRelation = new Relation(entity, relation.getEntityB(), relation.getType());
				relations.add(finalRelation);

				/*
				 * Here we check if this relation makes sense in the world. This
				 * check is done by another class, ConstraintCheck. No need to
				 * clutter up our code with checking logic here.
				 */
				if (!ConstraintCheck.isValidRelations(relations))
					throw new InterpretationException("error in move lol"); // TODO

				System.out.println("MOVE: Added new relation to relations: " + finalRelation);
				return finalRelation;
			case "relative":
				/*
				 * Relative has two children. The left child is always a
				 * relation type.
				 * 
				 * The right child is always either floor, basic_entity or
				 * relative_entity. For us, this means it is always an Entity.
				 */
				System.out.println("saw relative");
				Relation.TYPE relationType = (Relation.TYPE) walkTree(cterm.args[0]);
				entity = (Entity) walkTree(cterm.args[1]);
				return new Relation(new Entity(), entity, relationType);
			case "basic_entity":
				/*
				 * Basic_entity has two children. The left child is always a
				 * quantifier.
				 * 
				 * The right child is always an object. For us, this means it is
				 * an Entity.
				 */
				System.out.println("saw basic_entity");
				walkTree(cterm.args[0]);
				entity = (Entity) walkTree(cterm.args[1]);
				return entity;
			case "relative_entity":
				/*
				 * Relative_entity has three children. The left child is always
				 * a quantifier.
				 * 
				 * The middle child is always relative. For us, this means it is
				 * a Relation.
				 * 
				 * The right child is always an object. For us, this means it is
				 * an Entity.
				 */
				System.out.println("saw relative_entity");
				walkTree(cterm.args[0]);
				givenRelation = relation = (Relation) walkTree(cterm.args[2]);
				entity = (Entity) walkTree(cterm.args[1]);
				finalRelation = new Relation(entity, relation.getEntityB(), relation.getType());

				/*
				 * Here we check if this relation makes sense in the world. This
				 * check is done by another class, ConstraintCheck. No need to
				 * clutter up our code with checking logic here.
				 */
				if (!ConstraintCheck.isValidRelations(relations))
					throw new InterpretationException("error lol"); // TODO

				return finalRelation.getEntityA();
			case "object":
				/*
				 * Object always has three children. The left child is the size
				 * of the object.
				 * 
				 * The middle child is the form of the object.
				 * 
				 * The right child is the color of the object.
				 * 
				 * When this case has been reached from a relative_entity, the
				 * relation in relative_entity must be given to in this case so
				 * that we here can correctly decide what object we want.
				 * 
				 * This is done by setting the givenRelation object to the given
				 * relation.
				 */
				System.out.println("saw object");
				entity = new Entity(cterm.args[0].toString(), cterm.args[1].toString(), cterm.args[2].toString());

				List<Entity> matchedEntities = new ArrayList<>();

				if (givenRelation != null) {
					System.out.println("GivenRelation: " + givenRelation);
					// If a relation is given, we need to make sure that we only
					// match against objects that also match the given relation.
					for (List<Entity> column : world) {
						if (column.contains(entity)) {
							if (givenRelation.getType().equals(Relation.TYPE.ON_TOP_OF)) {
								if (givenRelation.getEntityB().getForm().equals(Entity.FORM.FLOOR)) {
									// The floor is a special case, since it is
									// not represented in our world.
									if (column.indexOf(entity) == 0)
										matchedEntities.add(column.get(column.indexOf(entity)));
								} else if (!givenRelation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
									// An entity is never on top of a box.
									if (column.indexOf(entity) > 0
											&& column.get(column.indexOf(entity) - 1).getForm()
													.equals(givenRelation.getEntityB().getForm()))
										// Check for entities below this entity.
										matchedEntities.add(column.get(column.indexOf(entity)));
								}
							} else if (givenRelation.getType().equals(Relation.TYPE.INSIDE)) {
								// Entities are always inside boxes, nothing
								// else. Only boxes.
								if (givenRelation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
									if (column.indexOf(entity) > 0
											&& column.get(column.indexOf(entity) - 1).getForm().equals(Entity.FORM.BOX))
										matchedEntities.add(column.get(column.indexOf(entity)));
								}
							} else if (givenRelation.getType().equals(Relation.TYPE.ABOVE)) {
								// Check for entities below this entity.
								for (int i = column.indexOf(entity); i >= 0; i--) {
									if (column.get(i).getForm().equals(givenRelation.getEntityB().getForm()))
										matchedEntities.add(column.get(column.indexOf(entity)));
								}
							} else if (givenRelation.getType().equals(Relation.TYPE.UNDER)) {
								// Check for entities above this entity.
								for (int i = column.indexOf(entity); i < column.size(); i++) {
									if (column.get(i).getForm().equals(givenRelation.getEntityB().getForm()))
										matchedEntities.add(column.get(column.indexOf(entity)));
								}
							} else if (givenRelation.getType().equals(Relation.TYPE.BESIDE)) {
								// Relation says the entity should be beside
								// another entity, is it?
								if (world.indexOf(column) + 1 < world.size()) {
									// Is it to the right of this entity?
									if (world.get(world.indexOf(column) + 1).contains(givenRelation.getEntityB())) {
										matchedEntities.add(column.get(column.indexOf(entity)));
									}
								} else if (world.indexOf(column) - 1 >= 0) {
									// Is is to the left of this entity?
									if (world.get(world.indexOf(column) - 1).contains(givenRelation.getEntityB())) {
										matchedEntities.add(column.get(column.indexOf(entity)));
									}
								}
							} else if (givenRelation.getType().equals(Relation.TYPE.LEFT_OF)) {
								// Relation says the entity should be left of
								// another entity, is it?
								for (int i = world.indexOf(column) + 1; i < world.size(); i++) {
									if (world.get(i).contains(givenRelation.getEntityB())) {
										matchedEntities.add(column.get(column.indexOf(entity)));
									}
								}
							} else if (givenRelation.getType().equals(Relation.TYPE.RIGHT_OF)) {
								// Relation says the entity should be right of
								// another entity, is it
								for (int i = world.indexOf(column) - 1; i >= 0; i--) {
									if (world.get(i).contains(givenRelation.getEntityB())) {
										matchedEntities.add(column.get(column.indexOf(entity)));
									}
								}
							}
						}
					}
				} else {
					// If no relation is given, we can match against any object.
					System.out.println("No relation given, matching " + entity + " against all objects in the world.");
					for (List<Entity> column : world) {
						if (column.contains(entity))
							matchedEntities.add(column.get(column.indexOf(entity)));
					}
				}

				if (matchedEntities.isEmpty()) {
					if (givenRelation == null)
						throw new InterpretationException("[" + entity + "] does not match anything in the world.");
					else
						throw new InterpretationException("The " + givenRelation + " relation does not match anything in the world.");
				} else if (matchedEntities.size() > 1) {
					// TODO: Handle Ambiguity Error in some fancy way. However,
					// we should save this for later.
				}

				System.out.println("Success: [" + entity + "] exists in the world as [" + matchedEntities.get(0) + "].");
				givenRelation = null; // Reset the givenRelation value.
				return matchedEntities.get(0);
			}
		} else if (term instanceof AtomTerm) {
			AtomTerm aterm = (AtomTerm) term;
			switch (aterm.value) {
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
