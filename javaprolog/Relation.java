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

	/* (non-Javadoc)
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

	/* (non-Javadoc)
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
	
}
