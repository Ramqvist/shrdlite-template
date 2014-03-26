/**
 * An Entity.
 * 
 */
public class Entity {

	public enum FORM {
		BRICK, PLANK, BALL, TABLE, PYRAMID, BOX
	}

	public enum SIZE {
		LARGE, SMALL
	}

	public enum COLOR {
		RED, BLACK, BLUE, GREEN, YELLOW, WHITE
	}

	private FORM form;
	private COLOR color;
	private SIZE size;

	public Entity(FORM form, SIZE size, COLOR color) {
		this.size = size;
		this.form = form;
		this.color = color;
	}

	public COLOR getColor() {
		return color;
	}

	public FORM getForm() {
		return form;
	}

	public SIZE getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "Entity: " + size + " " + color + " " + form;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((form == null) ? 0 : form.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Entity other = (Entity) obj;
		if (color != other.color) {
			return false;
		}
		if (form != other.form) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}
	
	
	
}
