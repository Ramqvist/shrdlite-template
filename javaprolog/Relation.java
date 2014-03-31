import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Object representing a relation between two objects.
 * 
 */
public class Relation {

	public enum TYPE {
		ON_TOP_OF, ABOVE, UNDER, BESIDE, LEFT_OF, RIGHT_OF, INSIDE, UNDEFINED
	}

	private TYPE type;
	private Entity a;
	private Entity b;

	public Relation(Entity a, Entity b, TYPE type) {
		this.a = a;
		this.b = b;
		this.type = type;
	}

	public Relation(Entity a, Entity b, String type) {
		this(a, b, parseType(type));
	}

	public static TYPE parseType(String type) {
		switch (type) {
		case "beside":
			return TYPE.BESIDE;
		case "leftof":
			return TYPE.LEFT_OF;
		case "rightof":
			return TYPE.RIGHT_OF;
		case "above":
			return TYPE.ABOVE;
		case "ontop":
			return TYPE.ON_TOP_OF;
		case "under":
			return TYPE.UNDER;
		case "inside":
			return TYPE.INSIDE;
		default:
			return TYPE.UNDEFINED;
		}
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public Entity getEntityA() {
		return a;
	}

	public void setEntityA(Entity a) {
		this.a = a;
	}

	public Entity getEntityB() {
		return b;
	}

	public void setEntityB(Entity b) {
		this.b = b;
	}

	public Relation copy() {
		return new Relation(a, b, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Relation))
			return false;
		Relation other = (Relation) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + type + " " + a + " " + b + ")";
	}

	/**
	 * Returns a list of all entities in the given world that match the given
	 * entity and relation.
	 * 
	 * @param entity
	 *            the Entity to match against a relation.
	 * @param relation
	 *            the Relation that describes the relation that should hold for
	 *            the given entity.
	 * @param world
	 *            a 2D array of entities that describes the world.
	 * @return a list of all the entities that match the given relation.
	 */
	public static List<Entity> matchEntityAndRelation(Entity entity, Relation relation, List<List<Entity>> world) {
		List<Entity> matchedEntities = new ArrayList<>();

		if (relation != null) {
			// If a relation is given, we need to make sure that we only
			// match against objects that also match the given relation.
			for (List<Entity> column : world) {
				if (column.contains(entity)) {
					if (relation.getType().equals(Relation.TYPE.ON_TOP_OF)) {
						if (relation.getEntityB().getForm().equals(Entity.FORM.FLOOR)) {
							// The floor is a special case, since it is
							// not represented in our world.
							if (column.indexOf(entity) == 0)
								matchedEntities.add(column.get(column.indexOf(entity)));
						} else if (!relation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
							// An entity is never on top of a box.
							if (column.indexOf(entity) > 0
									&& column.get(column.indexOf(entity) - 1).getForm().equals(relation.getEntityB().getForm()))
								// Check for entities below this entity.
								matchedEntities.add(column.get(column.indexOf(entity)));
						}
					} else if (relation.getType().equals(Relation.TYPE.INSIDE)) {
						// Entities are always inside boxes, nothing
						// else. Only boxes.
						if (relation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
							if (column.indexOf(entity) > 0 && column.get(column.indexOf(entity) - 1).getForm().equals(Entity.FORM.BOX)) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						}
					} else if (relation.getType().equals(Relation.TYPE.ABOVE)) {
						// Check for entities below this entity.
						for (int i = column.indexOf(entity); i >= 0; i--) {
							if (column.get(i).getForm().equals(relation.getEntityB().getForm()))
								matchedEntities.add(column.get(column.indexOf(entity)));
						}
					} else if (relation.getType().equals(Relation.TYPE.UNDER)) {
						// Check for entities above this entity.
						for (int i = column.indexOf(entity); i < column.size(); i++) {
							if (column.get(i).getForm().equals(relation.getEntityB().getForm()))
								matchedEntities.add(column.get(column.indexOf(entity)));
						}
					} else if (relation.getType().equals(Relation.TYPE.BESIDE)) {
						// Relation says the entity should be beside
						// another entity, is it?
						if (world.indexOf(column) + 1 < world.size()) {
							// Is it to the right of this entity?
							if (world.get(world.indexOf(column) + 1).contains(relation.getEntityB())) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						} else if (world.indexOf(column) - 1 >= 0) {
							// Is is to the left of this entity?
							if (world.get(world.indexOf(column) - 1).contains(relation.getEntityB())) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						}
					} else if (relation.getType().equals(Relation.TYPE.LEFT_OF)) {
						// Relation says the entity should be left of
						// another entity, is it?
						for (int i = world.indexOf(column) + 1; i < world.size(); i++) {
							if (world.get(i).contains(relation.getEntityB())) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						}
					} else if (relation.getType().equals(Relation.TYPE.RIGHT_OF)) {
						// Relation says the entity should be right of
						// another entity, is it
						for (int i = world.indexOf(column) - 1; i >= 0; i--) {
							if (world.get(i).contains(relation.getEntityB())) {
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
		return matchedEntities;
	}

	/**
	 * Returns a list of all entities in the given world that match the given
	 * entity and relation exactly, by using the equalsExact method of the
	 * Entity class.
	 * 
	 * @param entity
	 *            the Entity to match against a relation.
	 * @param relation
	 *            the Relation that describes the relation that should hold for
	 *            the given entity.
	 * @param world
	 *            a 2D array of entities that describes the world.
	 * @return a list of all the entities that match the given relation.
	 */
	public static List<Entity> matchEntityAndRelationExact(Entity entity, Relation relation, List<List<Entity>> world) {
		List<Entity> matchedEntities = new ArrayList<>();
		
		if (relation != null) {
			// If a relation is given, we need to make sure that we only
			// match against objects that also match the given relation.
			for (List<Entity> column : world) {
				if (entity.equalsExact(relation.getEntityA())) {
					if (relation.getType().equals(Relation.TYPE.ON_TOP_OF)) {
						if (relation.getEntityB().getForm().equals(Entity.FORM.FLOOR)) {
							// The floor is a special case, since it is
							// not represented in our world.
							if (column.indexOf(entity) == 0)
								matchedEntities.add(column.get(column.indexOf(entity)));
						} else if (!relation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
							// An entity is never on top of a box.
							if (column.indexOf(entity) > 0
									&& column.get(column.indexOf(entity) - 1).equalsExact(relation.getEntityB()))
								// Check for entities below this entity.
								matchedEntities.add(column.get(column.indexOf(entity)));
						}
					} else if (relation.getType().equals(Relation.TYPE.INSIDE)) {
						// Entities are always inside boxes, nothing
						// else. Only boxes.
						if (relation.getEntityB().getForm().equals(Entity.FORM.BOX)) {
							if (column.indexOf(entity) > 0 && column.get(column.indexOf(entity) - 1).equalsExact(relation.getEntityB())) {
//								System.out.println(column.get(column.indexOf(entity) - 1) + " matched " + relation.getEntityB());
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						}
					} else if (relation.getType().equals(Relation.TYPE.ABOVE)) {
						// Check for entities below this entity.
						for (int i = column.indexOf(entity); i >= 0; i--) {
							if (column.get(i).equalsExact(relation.getEntityB()))
								matchedEntities.add(column.get(column.indexOf(entity)));
						}
					} else if (relation.getType().equals(Relation.TYPE.UNDER)) {
						// Check for entities above this entity.
						for (int i = column.indexOf(entity); i < column.size(); i++) {
							if (column.get(i).equalsExact(relation.getEntityB()))
								matchedEntities.add(column.get(column.indexOf(entity)));
						}
					} else if (relation.getType().equals(Relation.TYPE.BESIDE)) {
						// Relation says the entity should be beside
						// another entity, is it?
						if (world.indexOf(column) + 1 < world.size()) {
							// Is it to the right of this entity?
							if (world.get(world.indexOf(column) + 1).contains(relation.getEntityB())) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						} else if (world.indexOf(column) - 1 >= 0) {
							// Is is to the left of this entity?
							if (world.get(world.indexOf(column) - 1).contains(relation.getEntityB())) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						}
					} else if (relation.getType().equals(Relation.TYPE.LEFT_OF)) {
						// Relation says the entity should be left of
						// another entity, is it?
						for (int i = world.indexOf(column) + 1; i < world.size(); i++) {
							if (world.get(i).contains(relation.getEntityB())) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						}
					} else if (relation.getType().equals(Relation.TYPE.RIGHT_OF)) {
						// Relation says the entity should be right of
						// another entity, is it
						for (int i = world.indexOf(column) - 1; i >= 0; i--) {
							if (world.get(i).contains(relation.getEntityB())) {
								matchedEntities.add(column.get(column.indexOf(entity)));
							}
						}
					}
				}
			}
		}
		return matchedEntities;
	}

}
