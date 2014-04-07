import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Allows you to interpret any tree parsed by the DCGParser, generating a list
 * of relations that describe the goal that the planner should try to reach.
 * 
 */
public class Interpreter {

	List<List<Entity>> world = new ArrayList<List<Entity>>();

	public Interpreter(JSONArray world, String holding, JSONObject objects) throws IOException {
		convertFromJSON(world, objects);
	}

	/**
	 * Converts the given JSON input to a two-dimensional list of {@link Entity}
	 * objects.
	 * 
	 * @param world
	 *            A JSONArray containing the entire state of the world.
	 * @param objects
	 *            A JSONObject containing mappings that describe every valid
	 *            object in the world.
	 * @throws IOException 
	 */
	private void convertFromJSON(JSONArray world, JSONObject objects) throws IOException {
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
		JSONToProlog(world,objects);

		Debug.print("World Representation");
		Debug.print(this.world);
		Debug.print();
	}

	public void JSONToProlog(JSONArray JSONWorld,JSONObject jObjects) throws IOException {

		PrintWriter world = new PrintWriter("world.pl", "UTF-8");
		PrintWriter objects = new PrintWriter("entities.pl", "UTF-8");

		String worldWithQuotesRemoved = JSONWorld.toString().replace("\"","");

		world.write("world(");
		world.write(worldWithQuotesRemoved);
		world.write(").");

		world.close();

		for(Object key : jObjects.keySet()) {
			String id = key.toString(); 
			Object value = jObjects.get(key);
			int index = 0;
			String str = value.toString();
			int begin = str.indexOf(":\"")+2;
			int end = str.indexOf("\",");
			index = end+1;

			String form = str.substring(begin,end);

			begin = str.indexOf(":\"",index)+2;
			end = str.indexOf("\",",index);
			index = end+1;
			
			String color = str.substring(begin,end);
			
			begin = str.indexOf(":\"",index)+2;
			end = str.indexOf("\"}",index);
			
			String size = str.substring(begin,end);

			objects.write("entity("+id+",");
			objects.write(form+",");
			objects.write(size+",");
			objects.write(color+").\n");
		}
		objects.close();
	}

	
	
	private List<Relation> relations;
	private List<Goal> goalList;

	/**
	 * Interprets the given tree, returning a (possibly empty) list of goals
	 * that satisfy the "meaning" of the given tree.
	 * 
	 * @param tree
	 *            A DCGParser tree of an utterance parsed by the Shrdlite
	 *            grammar.
	 * @return A (possibly empty) list of goals that model the goal of the given
	 *         tree.
	 */
	public List<Goal> interpret(Term tree) {
		goalList = new ArrayList<Goal>();
		relations = new ArrayList<>();

		Debug.print("=================");
		Debug.print("START OF INTERPRET");
		Debug.print();

		try {
			walkTree(tree);
			// goalList.add(new Goal(relations));
		} catch (InterpretationException e) {
			Debug.print(e);
		}

		Debug.print();
		Debug.print("END OF INTERPRET");
		Debug.print("================");
		Debug.print();

		if (goalList.size() > 0) {
//			System.out.print("There are " + goalList.size() + " items that match your query.");
//			for (Goal goal : goalList) {
//				String responseString = "";
//				for (int i = 0; i < goal.getRelations().size(); i++) {
//					if (i == goal.getRelations().size() - 1) {
//						responseString += goal.getRelations().get(i).getEntityA(); 
//					} else {
//						responseString += goal.getRelations().get(i).getEntityA() + ", ";
//					}
//				}
//				System.out.print(responseString);
//			}
			Debug.print(goalList);
		} else {	
			Debug.print("No goal could be produced.");
		}

		Debug.print();

		return goalList;
	}

	/*
	 * This is simply to avoid having to let walkTree have a second parameter
	 * that is null most of the time. Could perhaps be done better.
	 */
	private Relation givenRelation;

	/*
	 * These booleans are used to make sure that the correct relations are
	 * generated in the walkTree method. The logic is as follows:
	 * 
	 * As a rule, we only create and save relations if they are created in the
	 * "take", "put" and "move" methods.
	 * 
	 * However, sometimes we need to generate more than one relation, e.g. for
	 * the utterance "put the white ball in a red box on the floor" there is no
	 * red box on the floor. However, there is a red box that could be put on
	 * the floor. These booleans are then used to make sure that a relation that
	 * says that in addition to having the white ball be inside a red box, that
	 * red box should be on top of the floor.
	 */
	private boolean relativeChild = false, moveRelation = false;

	/*
	 * This is to handle the different quantifiers, like "any", "the" and "all".
	 */
	private String quantifier;

	/*
	 * This is used to store the second relations, for example when parsing the
	 * utterance "put a white ball in a box on the floor" this list will contain
	 * the "ontop box floor" relation.
	 */
	private List<Relation> secondRelations = new ArrayList<>();

	/**
	 * Recursively walks the given tree, generating a list of relations that
	 * describe the goal of the given tree.
	 * 
	 * @param term
	 *            A DCGParser representation of an utterance parsed by the
	 *            Shrdlite grammar.
	 * @return The final relation generated by this function.
	 * @throws InterpretationException
	 *             Thrown when the given tree doesn't match anything in the
	 *             world, or when the given tree doesn't satisfy the given
	 *             constraints of the world.
	 */
	public Object walkTree(Term term) throws InterpretationException {
		Relation relation, finalRelation;
		Entity entity;
		List<Entity> possibleEntities;
		List<Relation> relationList;

		if (term instanceof CompoundTerm) {
			CompoundTerm cterm = (CompoundTerm) term;
			switch (cterm.tag.functor.toString()) {
			case "take":
				/*
				 * Take has one child, which is always either floor,
				 * basic_entity or relative_entity.
				 */
				Debug.print("saw take");
				entity = (Entity) walkTree(cterm.args[0]);
				/*
				 * TODO: HOW DO WE EXPLAIN THIS AS A RELATION?
				 * 
				 * Quote from the course page:
				 * "For the take and put commands, the procedure is similar but different…"
				 */
				break;
			case "move":
				/*
				 * Move has two children. The left child is always either floor,
				 * basic_entity or relative_entity. For us, this means it is
				 * always an Entity.
				 * 
				 * The right child is always relative. For us, this means it is
				 * always a Relation.
				 */
				Debug.print("saw move");
				possibleEntities = (List<Entity>) walkTree(cterm.args[0]);
				moveRelation = true;
				relationList = (List<Relation>) walkTree(cterm.args[1]);
				moveRelation = false;
				Debug.print(relations);
				List<Relation> relationListAll = new ArrayList<>();
				for (Entity pentity : possibleEntities) {
					for (Relation arelation : relationList) {
						if (Relation.matchEntityAndRelationExact(pentity, arelation, world).isEmpty()) {
							if (Relation.matchEntityAndRelation(pentity, arelation, world).isEmpty() || quantifier.equals("all")) {
								relations = new ArrayList<Relation>();
								
								finalRelation = new Relation(pentity, arelation.getEntityB(), arelation.getType());
								addToRelations(finalRelation);
								
								for (Relation srelation : secondRelations) {
									if (srelation.getEntityA().equalsExact(finalRelation.getEntityB())) {
										addToRelations(srelation);
									}
								}
								
								/*
								 * Here we check if this relation makes sense in
								 * the world. This check is done by another
								 * class, ConstraintCheck. No need to clutter up
								 * our code with checking logic here.
								 */
								if (ConstraintCheck.isValidRelations(relations)) {
									if (quantifier.equals("all")) {
										relationListAll.addAll(relations);
									} else {
										if (relations.size() > 0) {
											goalList.add(new Goal(relations));
											Debug.print("Added a new goal: " + relations);
										}
									}
								}
							}
						}
					}
				}
				
				if (quantifier.equals("all")) {
					goalList.add(new Goal(relationListAll));
				}

				Debug.print("Returning from move");
				return null;
			case "relative":
				/*
				 * Relative has two children. The left child is always a
				 * relation type.
				 * 
				 * The right child is always either floor, basic_entity or
				 * relative_entity. For us, this means it is always an Entity.
				 */
				Debug.print("saw relative");
				Relation.TYPE relationType = (Relation.TYPE) walkTree(cterm.args[0]);
				relativeChild = true;

				possibleEntities = (List<Entity>) walkTree(cterm.args[1]);

				relationList = new ArrayList<Relation>();
				if (quantifier.equals("the")) {
					relationList.add(new Relation(new Entity(), possibleEntities.get(0), relationType));
				} else if (quantifier.equals("any") || quantifier.equals("all")) {
					for (Entity someEntity : possibleEntities) {
						relationList.add(new Relation(new Entity(), someEntity, relationType));
					}
				}
				Debug.print(relationList);
				Debug.print("Returning from relative");
				return relationList;
			case "basic_entity":
				/*
				 * Basic_entity has two children. The left child is always a
				 * quantifier.
				 * 
				 * The right child is always an object. For us, this means it is
				 * an Entity.
				 */
				Debug.print("saw basic_entity");
				quantifier = (String) walkTree(cterm.args[0]);
				Object anObject = walkTree(cterm.args[1]);
				Debug.print("Returning from basic_entity");
				return anObject;
			case "relative_entity":
				/*
				 * Relative_entity has three children. The left child is always
				 * a quantifier.
				 * 
				 * The middle child is always an object. For us, this means it
				 * is an Entity.
				 * 
				 * The right child is always relative. For us, this means it is
				 * a Relation.
				 */
				Debug.print("saw relative_entity");
				quantifier = (String) walkTree(cterm.args[0]);
				relationList = (List<Relation>) walkTree(cterm.args[2]);
				givenRelation = relation = (Relation) relationList.get(0);

				Debug.print("Relations " + relationList);
				possibleEntities = (List<Entity>) walkTree(cterm.args[1]);
				
				entity = possibleEntities.get(0); // TODO?????
				if (quantifier.equals("the")) {
					finalRelation = new Relation(entity, relation.getEntityB(), relation.getType());

					/*
					 * If this relative_entity has been reached from the right
					 * child of "move" and is a child of relative, we should
					 * save this relation.
					 */
					if (relativeChild && moveRelation) {
						Debug.print("relativeChild && moveRelation: Success! Added " + finalRelation + " to relations.");
						addToRelations(finalRelation);
					}

					/*
					 * Here we check if this relation makes sense in the world.
					 * This check is done by another class, ConstraintCheck. No
					 * need to clutter up our code with checking logic here.
					 */
					if (!ConstraintCheck.isValidRelations(relations)) {
						throw new InterpretationException("The created relation " + relations + " don't match the rules of the world.");
					}
				} else if (quantifier.equals("any") || quantifier.equals("all")) {
					for (Entity pentity : possibleEntities) {
						for (Relation arelation : relationList) {
							finalRelation = new Relation(pentity, arelation.getEntityB(), arelation.getType());

							/*
							 * If this relative_entity has been reached from the
							 * right child of "move" and is a child of relative,
							 * we should save this relation.
							 */
							if (relativeChild && moveRelation) {
								if (ConstraintCheck.isValidRelations(secondRelations)) {
									Debug.print("relativeChild && moveRelation: Success! Added " + finalRelation + " to relations.");
									secondRelations.add(finalRelation);
								}
							}
						}
					}
				}
				relativeChild = false;
				Debug.print("Returning from relative_entity");
				return possibleEntities;
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
				Debug.print("saw object");
				entity = new Entity(cterm.args[0].toString(), cterm.args[1].toString(), cterm.args[2].toString());

				/*
				 * Check if any entity matched the given relation, and act
				 * accordingly.
				 */
				List<Entity> matchedEntities = Relation.matchEntityAndRelation(entity, givenRelation, world);
				Object returnEntity;
				if (matchedEntities.isEmpty()) {
					if (givenRelation == null) {
						throw new InterpretationException("[" + entity + "] does not match anything in the world.");
					} else {
						returnEntity = matchEntity(world, givenRelation);
						if (returnEntity == null) {
							throw new InterpretationException("[" + entity + "] does not match anything in the world.");
						}
					}
				} else {
					returnEntity = matchedEntities;
				}

				Debug.print("Success: [" + entity + "] exists in the world as [" + returnEntity + "].");
				givenRelation = null; // Reset the givenRelation value.
				Debug.print("Returning from object");
				return returnEntity;
			}
		} else if (term instanceof AtomTerm) {
			AtomTerm aterm = (AtomTerm) term;
			switch (aterm.value) {
			case "floor":
				Debug.print("saw floor");
				List<Entity> tempList = new ArrayList<Entity>();
				tempList.add(new Entity(Entity.FORM.FLOOR, Entity.SIZE.UNDEFINED, Entity.COLOR.UNDEFINED));
				return tempList;
			case "the":
				Debug.print("saw the");
				// "The" means that there should only be one item that matches
				// the description. If more than one item matches the
				// description, we should ask clarification question(s):
				return "the";
			case "any":
				Debug.print("saw any");
				// "Any" should work as this currently does.
				return "any";
			case "all":
				Debug.print("saw all");
				// "All" should create one relation for each item that matches
				// the description.
				return "all";
			}
			// This static method handles the parsing of type values.
			return Relation.parseType(aterm.value);
		}
		Debug.print();
		return null;
	}

	/**
	 * This is called to add an item to the relation list. It makes sure that
	 * there are no relations created where an object is related to itself.
	 * 
	 * @param relation The relation to (maybe) add.
	 */
	private void addToRelations(Relation relation) {
		if (!relation.getEntityA().equalsExact(relation.getEntityB())) {
			relations.add(relation);
		}
	}

	/**
	 * Returns the first entity that matches the given relation in the given
	 * world.
	 * 
	 * @param world
	 *            a 2D list of entities.
	 * @param givenRelation
	 *            a Relation that describes the relation that the returned
	 *            entity should match.
	 * @return the matching entity or null if no entity matches the given
	 *         relation.
	 */
	private Entity matchEntity(List<List<Entity>> world, Relation givenRelation) {
		for (List<Entity> column : world) {
			for (Entity cEntity : column) {
				if (cEntity.equals(givenRelation.getEntityA())) {
					return cEntity;
				}
			}
		}
		return null;
	}

	public class InterpretationException extends Exception {

		private static final long serialVersionUID = 2280978916235342656L;

		public InterpretationException(String message) {
			super(message);
		}

	}

}
