/**
 * Object representing a relation between two objects.
 *
 */
public class Relation {
	
	public enum TYPE {
	    ON_TOP_OF, ABOVE, UNDER, BESIDE, LEFT_OF, RIGHT_OF
	}
	
	private TYPE type;
	private Entity a;
	private Entity b;
	
	public Relation(Entity a, Entity b, TYPE type) {
		this.a = a;
		this.b = b;
		this.type = type;
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
	
	public Entity getEntityB() {
		return b;
	}
	
}
