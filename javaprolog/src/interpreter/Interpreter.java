package src.interpreter;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import src.Debug;
import src.constraints.ConstraintCheck;
import src.world.Entity;
import src.world.Goal;
import src.world.Relation;
import src.world.Entity.COLOR;
import src.world.Entity.FORM;
import src.world.Entity.SIZE;
import src.world.Relation.TYPE;

/**
 * Allows you to interpret any tree parsed by the DCGParser, generating a list
 * of relations that describe the goal that the planner should try to reach.
 * 
 */
public class Interpreter {

	public List<List<Entity>> world = new ArrayList<List<Entity>>();
	public Entity heldEntity;

	public Interpreter(JSONArray world, String holding, JSONObject objects) throws IOException {
		convertFromJSON(world, objects, holding);
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
	private void convertFromJSON(JSONArray world, JSONObject objects, String holding) throws IOException {
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
		JSONToProlog(world, objects);

		if (holding != null) {
			JSONObject description = (JSONObject) objects.get(holding);
			heldEntity = new Entity((String) description.get("form"), (String) description.get("size"), (String) description.get("color"));
		}

		Debug.print("World Representation");
		Debug.print(this.world);
		Debug.print();

		Debug.print("Held Entity");
		if (heldEntity != null) {
			Debug.print(heldEntity);
		} else {
			Debug.print("Nothing");
		}
		Debug.print();
	}

	public void JSONToProlog(JSONArray JSONWorld, JSONObject jObjects) throws IOException {

		PrintWriter world = new PrintWriter("world.pl", "UTF-8");
		PrintWriter objects = new PrintWriter("entities.pl", "UTF-8");

		String worldWithQuotesRemoved = JSONWorld.toString().replace("\"", "");

		world.write("world(");
		world.write(worldWithQuotesRemoved);
		world.write(").");

		world.close();

		for (Object key : jObjects.keySet()) {
			String id = key.toString();
			Object value = jObjects.get(key);
			int index = 0;
			String str = value.toString();
			int begin = str.indexOf(":\"") + 2;
			int end = str.indexOf("\",");
			index = end + 1;

			String form = str.substring(begin, end);

			begin = str.indexOf(":\"", index) + 2;
			end = str.indexOf("\",", index);
			index = end + 1;

			String color = str.substring(begin, end);

			begin = str.indexOf(":\"", index) + 2;
			end = str.indexOf("\"}", index);

			String size = str.substring(begin, end);

			objects.write("entity(" + id + ",");
			objects.write(form + ",");
			objects.write(size + ",");
			objects.write(color + ").\n");
		}
		objects.close();
	}
	
	private boolean cancel;

	public boolean checkForCancel(Term tree) {
		cancel = false;
		goalList = new ArrayList<>();
		try {
			walkTree(tree);
		} catch (InterpretationException e) {
			Debug.print("Error checking for cancel: " + e);
		}
		return cancel;
	}
	
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
		goalList = new ArrayList<>();

		Debug.print("=================");
		Debug.print("START OF INTERPRET");
		Debug.print();

		try {
			walkTree(tree);
		} catch (InterpretationException e) {
			Debug.print(e);
		}

		Debug.print();
		Debug.print("END OF INTERPRET");
		Debug.print("================");
		Debug.print();

		if (goalList.size() > 0) {
			for (Goal goal : goalList) {
				Debug.print(goal);
			}
		} else {
			Debug.print("No goal could be produced.");
		}
		Debug.print();
		return goalList;
	}

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
		if (term instanceof CompoundTerm) {
			CompoundTerm cterm = (CompoundTerm) term;
			return walkCompoundTerm(cterm);
		} else if (term instanceof AtomTerm) {
			AtomTerm aterm = (AtomTerm) term;
			return walkAtomTerm(aterm);
		}
		Debug.print();
		return null;
	}
	
	private Object walkCompoundTerm(CompoundTerm cterm) throws InterpretationException {
		switch (cterm.tag.functor.toString()) {
		case "answer":
			return answer(cterm);
		case "take":
			return take(cterm);
		case "move":
			return move(cterm);
		case "relative":
			return relative(cterm);
		case "basic_entity":
			return basicEntity(cterm);
		case "relative_entity":
			return relativeEntity(cterm);
		case "object":
			return object(cterm);
		}
		return null;
	}

	private Object walkAtomTerm(AtomTerm aterm) throws InterpretationException {
		switch (aterm.value) {
		case "floor":
			return floor();
		case "the":
			Debug.print("saw the");
			return "the";
		case "any":
			Debug.print("saw any");
			return "any";
		case "all":
			Debug.print("saw all");
			return "all";
		case "nevermind":
			cancel = true;
		}
		// This static method handles the parsing of type values.
		return Relation.parseType(aterm.value);
	}
	
	private Object answer(CompoundTerm cterm) throws InterpretationException {
		Debug.print("saw answer");
		List<List<Pair<Entity, Relation>>> matchedEntitiesList = (List<List<Pair<Entity, Relation>>>) walkTree(cterm.args[0]);
		for (List<Pair<Entity, Relation>> matchedEntities : matchedEntitiesList) {
			for (Pair<Entity, Relation> matchedEntity : matchedEntities) {
				goalList.add(new Goal(new Relation(matchedEntity.a, new Entity(), Relation.TYPE.UNDEFINED)));
			}
		}
		return null;
	}
	
	/**
	 * Take has one child, which is always either floor, basic_entity or
	 * relative_entity.
	 * 
	 * @param cterm
	 *            The node to be parsed.
	 * @return Null.
	 * @throws InterpretationException
	 *             if no matching entities could be found.
	 */
	private Object take(CompoundTerm cterm) throws InterpretationException {
		Debug.print("saw take");
		List<List<Pair<Entity, Relation>>> matchedEntitiesList = (List<List<Pair<Entity, Relation>>>) walkTree(cterm.args[0]);
		for (List<Pair<Entity, Relation>> matchedEntities : matchedEntitiesList) {
			for (Pair<Entity, Relation> matchedEntity : matchedEntities) {
				if (matchedEntity.a.getForm() == Entity.FORM.FLOOR) {
					throw new InterpretationException("You can't take the floor, stupid.");
				}
				goalList.add(new Goal(new Relation(matchedEntity.a, new Entity(), Relation.TYPE.HELD)));
			}
		}
		return null;
	}

	/**
	 * Move has two children. The left child is always either floor,
	 * basic_entity or relative_entity. For us, this means it is always an
	 * Entity.
	 * 
	 * The right child is always relative. For us, this means it is always a
	 * Relation.
	 * 
	 * @param cterm
	 *            The node to be parsed.
	 * @return Null.
	 * @throws InterpretationException
	 *             if no matching entities could be found.
	 */
	private Object move(CompoundTerm cterm) throws InterpretationException {
		Debug.print("saw move");

		List<List<Pair<Entity, Relation>>> matchedEntitiesList = (List<List<Pair<Entity, Relation>>>) walkTree(cterm.args[0]);
		Debug.print("move: matchedEntitiesList: " + matchedEntitiesList);

		// There is no need to continue if no matched entities could be found. Also, we only care about the new relations possibly created
		// if they are created on the right side.
		if (matchedEntitiesList.isEmpty() || matchedEntitiesList.get(0).get(0).b != null) {
			return null;
		}
		
		List<List<List<Relation>>> relationsList = (List<List<List<Relation>>>) walkTree(cterm.args[1]);
		Debug.print("move: relationsList: " + relationsList);
		
		if (relationsList.isEmpty()) {
			return null;
		}

		// Check if "a" inside / on top of "all".
		if (matchedEntitiesList.size() >= 1 && relationsList.size() == 1) {
			if (matchedEntitiesList.get(0).size() == 1 && relationsList.get(0).size() > 1) {
				if (relationsList.get(0).get(0).get(0).getType() == Relation.TYPE.INSIDE
						|| relationsList.get(0).get(0).get(0).getType() == Relation.TYPE.ON_TOP_OF) {
					// TODO: Try to fix or just deny?
					return null;
				}
			}
		}

		boolean isInsideOrOnTopOf = true;
		
		// Check if the relation to create is "all" inside / on top of "a" and
		// fix these.
		if (matchedEntitiesList.size() == 1 && relationsList.size() >= 1) {
			if (matchedEntitiesList.get(0).size() > 1 && relationsList.get(0).size() == 1) {
				if (relationsList.get(0).get(0).get(0).getType() == Relation.TYPE.INSIDE
						|| relationsList.get(0).get(0).get(0).getType() == Relation.TYPE.ON_TOP_OF) {
					List<List<Relation>> tempList = new ArrayList<>();
					for (List<List<Relation>> relationList : relationsList) {
						tempList.add(relationList.get(0));
					}
					relationsList.clear();
					relationsList.add(tempList);
					Debug.print("move: found an \"all -> a\" relation, fixed it. New relationsList: " + relationsList);
					if (relationsList.get(0).get(0).get(0).getType() == Relation.TYPE.ON_TOP_OF
							&& relationsList.get(0).get(0).get(0).getEntityB().getForm() == Entity.FORM.FLOOR) {
						isInsideOrOnTopOf = false;
					}
				} else {
					isInsideOrOnTopOf = false;
				}
			}
		}
		
		// An entity cannot be related to itself. Therefore, we remove all relations that relate to any matched entity.
		List<List<List<Relation>>> tempRelationsList = relationsList;
		relationsList = removeRelationsRelatingToMatched(matchedEntitiesList, relationsList);
		if (relationsList.isEmpty()) {
			relationsList = tempRelationsList;
			List<List<Pair<Entity, Relation>>> tempMatchedEntitiesList = matchedEntitiesList;
			matchedEntitiesList = removeEntitiesRelatingToRelations(matchedEntitiesList, relationsList);
			if (matchedEntitiesList.isEmpty()) {
				matchedEntitiesList = tempMatchedEntitiesList;
			}
		}
		Debug.print("move: Removed relations relating to matched entitites. relationsList: " + relationsList);
		
		for (List<Pair<Entity, Relation>> matchedEntities : matchedEntitiesList) {
			int matchedEntitiesSize;
			if (isInsideOrOnTopOf) {
				matchedEntitiesSize = matchedEntities.size();
			} else {
				matchedEntitiesSize = 1;
			}
			for (List<List<Relation>> relationListList : relationsList) {
				int relationListListSize;
				if (isInsideOrOnTopOf) {
					relationListListSize = relationListList.size();
				} else {
					relationListListSize = 1;
				}
				for (int i = 0; i < matchedEntitiesSize; i++) {
					for (int j = 0; j < relationListListSize; j++) {
						Goal tempGoal = createGoalRelations(matchedEntities, relationListList);
						if (tempGoal != null && !goalList.contains(tempGoal)) {
							if (ConstraintCheck.isValidRelations(tempGoal.getRelations()) && isNotDuplicateGoal(tempGoal)) {
								goalList.add(tempGoal);
								Debug.print("move: OK, the newly created goals were okay to add. So, added: " + tempGoal);
							}
						}
						// Move last item to the front.
						List<Relation> removed = relationListList.remove(relationListList.size() - 1);
						relationListList.add(0, removed);
					}
					// Move last item to the front.
					Pair<Entity, Relation> removed = matchedEntities.remove(matchedEntities.size() - 1);
					matchedEntities.add(0, removed);
				}
			}
		}
		return null;
	}
	
	private List<List<List<Relation>>> removeRelationsRelatingToMatched(List<List<Pair<Entity, Relation>>> matchedEntitiesList, List<List<List<Relation>>> relationsList) {
		List<List<List<Relation>>> tempRelationsList = new ArrayList<>();
		for (List<List<Relation>> relationListList : relationsList) {
			List<List<Relation>> tempRelationListList = new ArrayList<>();
			for (List<Relation> relationList : relationListList) {
				List<Relation> tempRelationList = new ArrayList<>(relationList);
				for (List<Pair<Entity, Relation>> matchedEntities : matchedEntitiesList) {
					for (Pair<Entity, Relation> matchedEntityPair : matchedEntities) {
						if (relationList.get(0).getEntityB().equalsExact(matchedEntityPair.a)) {
							for (Relation relation : relationList) {
								tempRelationList.remove(relation);
							}
						}
					}
				}
				if (!tempRelationList.isEmpty()) {
					tempRelationListList.add(tempRelationList);
				}
			}
			if (!tempRelationListList.isEmpty()) {
				tempRelationsList.add(tempRelationListList);
			}
		}
		return tempRelationsList;
	}
	
	private List<List<Pair<Entity, Relation>>> removeEntitiesRelatingToRelations(List<List<Pair<Entity, Relation>>> matchedEntitiesList, List<List<List<Relation>>> relationsList) {
		List<List<Pair<Entity, Relation>>> tempMatchedEntitiesList = new ArrayList<>();
		for (List<Pair<Entity, Relation>> matchedEntities : matchedEntitiesList) {
			List<Pair<Entity, Relation>> tempMatchedEntities = new ArrayList<>(matchedEntities);
			for (Pair<Entity, Relation> matchedEntityPair : matchedEntities) {
				for (List<List<Relation>> relationListList : relationsList) {
					for (List<Relation> relationList : relationListList) {
						for (Relation relation : relationList) {
							if (relation.getEntityB().equalsExact(matchedEntityPair.a)) {
								tempMatchedEntities.remove(matchedEntityPair);
							}
						}
					}
				}
			}
			if (!tempMatchedEntities.isEmpty()) {
				tempMatchedEntitiesList.add(tempMatchedEntities);
			}
		}
		return tempMatchedEntitiesList;
	}
	
	private Goal createGoalRelations(List<Pair<Entity, Relation>> matchedEntities, List<List<Relation>> relationListList) {
		Goal tempGoal = null;
		List<Relation> goalRelationList = createRelations(matchedEntities, relationListList);
		Debug.print("move: goalRelationList is now finished for " + "matchedEntityPair.");
		Debug.print("move: goalRelationList: " + goalRelationList);
		Debug.print("move: Is " + goalRelationList + " OK to add?");
		int countOfRelationsToSelf = 0;
		for (Pair<Entity, Relation> matchedEntity : matchedEntities) {
			for (List<Relation> relationList : relationListList) {
				for (Relation relation : relationList) {
					if (relation.getEntityB().equalsExact(matchedEntity.a)) {
						Debug.print(matchedEntity.a + " exists in relationList.");
						countOfRelationsToSelf++;
					}
				}
			}
		}
		if (goalRelationList.size() < relationListList.size() - countOfRelationsToSelf) {
			Debug.print("move: No, " + goalRelationList + " wasn't OK. Ignoring it.");
			goalRelationList.clear();
			
		}
		if (!goalRelationList.isEmpty()) {
			Debug.print("move: Yes it was!");
			tempGoal = new Goal(goalRelationList);
			Debug.print("move: Added to goalList, which is now: " + goalList);
		}
//		for (Pair<Entity, Relation> matchedEntity : matchedEntities) {
//			if (matchedEntity.quantifier != null) {
//				if (matchedEntity.quantifier.equalsIgnoreCase("the")) {
//					tempGoal.quantifier = "the";
//				}
//			}
//		}
		return tempGoal;
	}
	
	private List<Relation> createRelations(List<Pair<Entity, Relation>> matchedEntities, List<List<Relation>> relationListList) {
		List<Relation> goalRelationList = new ArrayList<>();
		for (Pair<Entity, Relation> matchedEntityPair : matchedEntities) {
			Debug.print("move: matchedEntityPair: " + matchedEntityPair);
			for (List<Relation> relationList : relationListList) {
				Debug.print("move: relationList: " + relationList);
				if (relationList.get(0).getEntityA().getForm() == Entity.FORM.UNDEFINED) {
					Relation newRelation = new Relation(matchedEntityPair.a, relationList.get(0).getEntityB(), relationList.get(0).getType());
					if (checkRelation(newRelation, goalRelationList)) {
						Debug.print("move: added " + newRelation);
						goalRelationList.add(newRelation);
						if (relationList.size() > 1) {
							for (int i = 1; i < relationList.size(); i++) {
								if (checkRelation(relationList.get(i), goalRelationList)) {
									goalRelationList.add(relationList.get(i));
									Debug.print("move: Added an extra relation: " + relationList.get(i));
								}
							}
						}
					}
				}
				Debug.print("move: goalRelationList: " + goalRelationList);
			}
		}
		return goalRelationList;
	}
	
	/**
	 * Checks if the given relation is OK in regards to a given list of
	 * relations.
	 * 
	 * @param relation
	 *            The Relation to check.
	 * @param relationList
	 *            A list of relation to check the relation against.
	 * @return True if the given relation is OK, otherwise false.
	 */
	private boolean checkRelation(Relation relation, List<Relation> relationList) {
		if (!ConstraintCheck.isValidRelation(relation)) {
			Debug.print("checkRelation: " + relation + " is false cause it isn't valid.");
			return false;
		}
		if (relation.getType() == Relation.TYPE.INSIDE || relation.getType() == Relation.TYPE.ON_TOP_OF) {
			for (Relation anotherRelation : relationList) {
				if (anotherRelation.getType() == relation.getType()) {
					if (anotherRelation.getEntityB().equalsExact(relation.getEntityB())
							|| anotherRelation.getEntityA().equalsExact(relation.getEntityA())) {
						if (anotherRelation.getEntityB().getForm() != Entity.FORM.FLOOR
								&& relation.getEntityB().getForm() != Entity.FORM.FLOOR) {
							Debug.print("checkRelation: " + relation + " is false cause " + anotherRelation + " already exists.");
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean isNotDuplicateGoal(Goal goal) {
		for (Goal existingGoal : goalList) {
			List<Relation> existingGoalRelations = new ArrayList<>(existingGoal.getRelations());
			existingGoalRelations.retainAll(goal.getRelations());
			if (existingGoalRelations.containsAll(goal.getRelations())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Relative has two children. The left child is always a relation type.
	 * 
	 * The right child is always either floor, basic_entity or relative_entity.
	 * For us, this means it is always an Entity.
	 * 
	 * @param cterm
	 *            The node to be parsed.
	 * @return A list of relations.
	 * @throws InterpretationException
	 *             if no matching entities could be found.
	 */
	private Object relative(CompoundTerm cterm) throws InterpretationException {
		Debug.print("saw relative");
		Relation.TYPE relationType = (Relation.TYPE) walkTree(cterm.args[0]);
		Debug.print("relative: relationType: " + relationType);

		List<List<Pair<Entity, Relation>>> matchedEntitiesList = (List<List<Pair<Entity, Relation>>>) walkTree(cterm.args[1]);
		Debug.print("relative: matchedEntitiesList: " + matchedEntitiesList);

		List<List<List<Relation>>> relationsList = new ArrayList<>();
		for (List<Pair<Entity, Relation>> matchedEntities : matchedEntitiesList) {
			List<List<Relation>> tempListList = new ArrayList<>();
			for (Pair<Entity, Relation> someEntity : matchedEntities) {
				List<Relation> tempList = new ArrayList<>();
				tempList.add(new Relation(new Entity(), someEntity.a, relationType));
				if (someEntity.b != null) {
					tempList.add(someEntity.b);
				}
				tempListList.add(tempList);
			}
			relationsList.add(tempListList);
		}
		Debug.print("relative: relationsList: " + relationsList);
		return relationsList;
	}

	/**
	 * Basic_entity has two children. The left child is always a quantifier.
	 * 
	 * The right child is always an object. For us, this means it is an Entity.
	 * 
	 * @param cterm
	 *            The node to be interpreted.
	 * @return A list of all matching entities.
	 * @throws InterpretationException
	 *             if no matching entities could be found.
	 */
	private Object basicEntity(CompoundTerm cterm) throws InterpretationException {
		// TODO: This should only return the entities that we get from "object",
		// also check the quantifier and change which entities to return.
		Debug.print("saw basic_entity");
		String quantifier = (String) walkTree(cterm.args[0]);
		Debug.print("basicEntity: quantifier: " + quantifier);

		List<Entity> matchedEntities = (List<Entity>) walkTree(cterm.args[1]);
		Debug.print("basicEntity: matchedEntities: " + matchedEntities);

		List<List<Pair<Entity, Relation>>> matchedEntitiesPruned = new ArrayList<>();
		if (quantifier.equals("the")) {
//			Entity temp = matchedEntities.get(0);
//			Pair<Entity, Relation> pair = new Pair<>(temp, null);
//			List<Pair<Entity, Relation>> matchedEntitiesPair = new ArrayList<>();
//			matchedEntitiesPair.add(pair);
//			matchedEntitiesPruned.add(matchedEntitiesPair);
			for (Entity matchedEntity : matchedEntities) {
				Pair<Entity, Relation> pair = new Pair<>(matchedEntity, null);
//				pair.quantifier = "the";
				List<Pair<Entity, Relation>> matchedEntitiesPair = new ArrayList<>();
				matchedEntitiesPair.add(pair);
				matchedEntitiesPruned.add(matchedEntitiesPair);
			}
		} else if (quantifier.equals("any")) {
			for (Entity matchedEntity : matchedEntities) {
				Pair<Entity, Relation> pair = new Pair<>(matchedEntity, null);
				List<Pair<Entity, Relation>> matchedEntitiesPair = new ArrayList<>();
				matchedEntitiesPair.add(pair);
				matchedEntitiesPruned.add(matchedEntitiesPair);
			}
		} else if (quantifier.equals("all")) {
			List<Pair<Entity, Relation>> matchedEntitiesPair = new ArrayList<>();
			for (Entity matchedEntity : matchedEntities) {
				Pair<Entity, Relation> pair = new Pair<>(matchedEntity, null);
				matchedEntitiesPair.add(pair);
			}
			matchedEntitiesPruned.add(matchedEntitiesPair);
		}
		Debug.print("basicEntity: " + quantifier + ": picked " + matchedEntitiesPruned);
		return matchedEntitiesPruned;
	}

	/**
	 * Relative_entity has three children. The left child is always a
	 * quantifier.
	 * 
	 * The middle child is always an object. For us, this means it is an Entity.
	 * 
	 * The right child is always relative. For us, this means it is a Relation.
	 * 
	 * @param cterm
	 *            The node to be interpreted.
	 * @return A list of all matching entities.
	 * @throws InterpretationException
	 *             if no entities could be found.
	 */
	private Object relativeEntity(CompoundTerm cterm) throws InterpretationException {
		Debug.print("saw relative_entity");
		String quantifier = (String) walkTree(cterm.args[0]);
		Debug.print("relativeEntity: quantifier: " + quantifier);

		List<List<List<Relation>>> relationsList = (List<List<List<Relation>>>) walkTree(cterm.args[2]);
		Debug.print("relativeEntity: relationList: " + relationsList);

		List<Entity> matchedEntities = (List<Entity>) walkTree(cterm.args[1]);
		List<Entity> matchedEntitiesPrePrune = new ArrayList<>(matchedEntities);
		Debug.print("relativeEntity: matchedEntities pre-prune: " + matchedEntities);

		// We can first prune away all the entities that cannot be applied to
		// any of the relations. Balls under objects etc.
		List<Entity> tempEntityList = new ArrayList<>();
		for (List<List<Relation>> relationListList : relationsList) {
			for (List<Relation> relationList : relationListList) {
				List<Entity> temp = matchEntitiesAndRelations(world, matchedEntities, relationList, heldEntity);
				if (!tempEntityList.containsAll(temp)) {
					tempEntityList.addAll(temp);
				}
			}
		}
		matchedEntities = tempEntityList;
		Debug.print("relativeEntity: matchedEntities post-prune: " + matchedEntities);

		List<Pair<Entity, Relation>> matchedEntitiesPair = new ArrayList<>();
		if (matchedEntities.isEmpty()) {
			for (List<List<Relation>> relationListList : relationsList) {
				for (List<Relation> relationList : relationListList) {
					for (Relation relation : relationList) {
						for (Entity entity : matchedEntitiesPrePrune) {
							Relation tempRelation = new Relation(entity, relation.getEntityB(), relation.getType());
							if (ConstraintCheck.isValidRelation(tempRelation)) {
								matchedEntitiesPair.add(new Pair<Entity, Relation>(entity, tempRelation));
							}
						}
						Debug.print(relation);
					}
				}
			}
		} else {
			for (Entity entity : matchedEntities) {
				matchedEntitiesPair.add(new Pair<Entity, Relation>(entity, null));
			}
		}
		Debug.print("relativeEntity: matchedEntitiesPair: " + matchedEntitiesPair);

		List<List<Pair<Entity, Relation>>> matchedEntitiesPruned = new ArrayList<>();
		if (quantifier.equals("the")) {
//			Pair<Entity, Relation> temp = matchedEntitiesPair.get(0);
//			matchedEntitiesPair.clear();
//			matchedEntitiesPair.add(temp);
//			matchedEntitiesPruned.add(matchedEntitiesPair);
			for (Pair<Entity, Relation> matchedEntityPair : matchedEntitiesPair) {
//				matchedEntityPair.quantifier = "the";
				List<Pair<Entity, Relation>> tempMatchedEntitiesPair = new ArrayList<>();
				tempMatchedEntitiesPair.add(matchedEntityPair);
				matchedEntitiesPruned.add(tempMatchedEntitiesPair);
			}
		} else if (quantifier.equals("any")) {
			for (Pair<Entity, Relation> matchedEntityPair : matchedEntitiesPair) {
				List<Pair<Entity, Relation>> tempMatchedEntitiesPair = new ArrayList<>();
				tempMatchedEntitiesPair.add(matchedEntityPair);
				matchedEntitiesPruned.add(tempMatchedEntitiesPair);
			}
		} else if (quantifier.equals("all")) {
			matchedEntitiesPruned.add(matchedEntitiesPair);
		}
		Debug.print("relativeEntity: " + quantifier + ": picked " + matchedEntitiesPruned);
		return matchedEntitiesPruned;
	}

	/**
	 * Object always has three children. The left child is the size of the
	 * object.
	 * 
	 * The middle child is the form of the object.
	 * 
	 * The right child is the color of the object.
	 * 
	 * @param cterm
	 *            The node to be interpreted.
	 * @return A list of matching entities.
	 * @throws InterpretationException
	 *             if no entities could be matched.
	 */
	private Object object(CompoundTerm cterm) throws InterpretationException {
		Debug.print("saw object");
		Entity entity = new Entity(cterm.args[0].toString(), cterm.args[1].toString(), cterm.args[2].toString());
		List<Entity> matchedEntities = matchEntity(world, entity);
		Debug.print("object: matchedEntities: " + matchedEntities);

		if (matchedEntities.isEmpty()) {
			throw new InterpretationException(entity + " does not match anything in the world.");
		}

		return matchedEntities;
	}

	private List<Entity> matchEntity(List<List<Entity>> world, Entity entity) {
		List<Entity> matchedEntities = new ArrayList<>();
		for (List<Entity> column : world) {
			for (Entity cEntity : column) {
				if (cEntity.equals(entity)) {
					matchedEntities.add(cEntity);
				}
			}
		}
		if (heldEntity != null && heldEntity.equals(entity)) {
			matchedEntities.add(heldEntity);
		}
		return matchedEntities;
	}

	private List<List<Pair<Entity, Relation>>> floor() {
		Debug.print("saw floor");
		List<List<Pair<Entity, Relation>>> tempListList = new ArrayList<>();
		List<Pair<Entity, Relation>> tempList = new ArrayList<>();
		tempList.add(new Pair<Entity, Relation>(new Entity(Entity.FORM.FLOOR, Entity.SIZE.UNDEFINED, Entity.COLOR.UNDEFINED), null));
		tempListList.add(tempList);
		return tempListList;
	}
	
	private List<Entity> matchEntitiesAndRelations(List<List<Entity>> world, List<Entity> entities, List<Relation> relations,
			Entity heldEntity) {
		List<Entity> matchedEntities = new ArrayList<>();
		for (Entity entity : entities) {
			boolean matched = true;
			for (Relation relation : relations) {
				if (!checkEntityAndRelation(world, entity, relation)) {
					matched = false;
				}
			}
			if (matched) {
				matchedEntities.add(entity);
			}
		}
		for (Entity entity : entities) {
			boolean matched = true;
			if (heldEntity != null && heldEntity.equals(entity)) {
				for (Relation relation : relations) {
					if (!checkEntityAndRelation(world, heldEntity, relation)) {
						matched = false;
					}
				}
				if (matched) {
					matchedEntities.add(entity);
				}
			}
		}
		return matchedEntities;
	}

	private boolean checkEntityAndRelation(List<List<Entity>> world, Entity entity, Relation relation) {
		for (List<Entity> column : world) {
			if (relation.getType().equals(Relation.TYPE.ON_TOP_OF)) {
				if (relation.getEntityB().getForm().equals(Entity.FORM.FLOOR)) {
					// The floor is a special case, since it is
					// not represented in our world.
					if (column.indexOf(entity) == 0) {
						return true;
					}
				} else if (!relation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
					// An entity is never on top of a box.
					// Check for entities below this entity.
					if (column.contains(entity) && column.indexOf(entity) > 0) {
						if (column.get(column.indexOf(entity) - 1).equals(relation.getEntityB())) {
							return true;
						}
					}
				}
			} else if (relation.getType().equals(Relation.TYPE.INSIDE)) {
				// Entities are always inside boxes, nothing
				// else. Only boxes.
				if (relation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
					if (column.contains(entity) && column.indexOf(entity) > 0) {
						if (column.get(column.indexOf(entity) - 1).equals(relation.getEntityB())) {
							return true;
						}
					}
				}
			} else if (relation.getType().equals(Relation.TYPE.ABOVE)) {
				// Check for entities below this entity.
				if (column.contains(entity)) {
					for (int i = column.indexOf(entity); i >= 0; i--) {
						if (column.get(i).equals(relation.getEntityB())) {
							return true;
						}
					}
				}
			} else if (relation.getType().equals(Relation.TYPE.UNDER)) {
				// Check for entities above this entity.
				if (column.contains(entity)) {
					for (int i = column.indexOf(entity) + 1; i < column.size(); i++) {
						if (column.get(i).equals(relation.getEntityB())) {
							Debug.print(entity + " is UNDER " + relation.getEntityB());
							return true;
						}
					}
				}
			} else if (relation.getType().equals(Relation.TYPE.BESIDE)) {
				// Relation says the entity should be beside
				// another entity, is it?
				if (column.contains(entity)) {
					if (world.indexOf(column) + 1 < world.size()) {
						// Is it to the right of this entity?
						if (world.get(world.indexOf(column) + 1).contains(relation.getEntityB())) {
							return true;
						}
					}
					if (world.indexOf(column) - 1 >= 0) {
						// Is is to the left of this entity?
						if (world.get(world.indexOf(column) - 1).contains(relation.getEntityB())) {
							return true;
						}
					}
				}
			} else if (relation.getType().equals(Relation.TYPE.LEFT_OF)) {
				// Relation says the entity should be left of
				// another entity, is it?
				if (column.contains(entity)) {
					for (int i = world.indexOf(column) + 1; i < world.size(); i++) {
						if (world.get(i).contains(relation.getEntityB())) {
							return true;
						}
					}
				}
			} else if (relation.getType().equals(Relation.TYPE.RIGHT_OF)) {
				// Relation says the entity should be right of
				// another entity, is it
				if (column.contains(entity)) {
					for (int i = world.indexOf(column) - 1; i >= 0; i--) {
						if (world.get(i).contains(relation.getEntityB())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private class Pair<A, B> {
		
		public final A a;
		public final B b;
		public String quantifier;
		
		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
		
		@Override
		public String toString() {
			return "Pair<" + a + ", " + b + ">";
		}
		
	}
	
	public class InterpretationException extends Exception {

		private static final long serialVersionUID = 2280978916235342656L;

		public InterpretationException(String message) {
			super(message);
		}

	}

}
