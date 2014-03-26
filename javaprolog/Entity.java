/**
 * An Entity.
 * 
 */
public class Entity {

	public enum FORM {
		BRICK, PLANK, BALL, TABLE, PYRAMID, BOX, FLOOR
	}

	public enum SIZE {
		LARGE, SMALL, UNDEFINED
	}

	public enum COLOR {
		RED, BLACK, BLUE, GREEN, YELLOW, WHITE, UNDEFINED
	}

	private FORM form;
	private COLOR color;
	private SIZE size;

	public Entity(FORM form, SIZE size, COLOR color) {
		this.size = size;
		this.form = form;
		this.color = color;
	}
	
	public Entity(String form, String size, String color) {
		switch(form) {
		case "brick":
			this.form = Entity.FORM.BRICK;
			break;
		case "plank":
			this.form = Entity.FORM.PLANK;
			break;
		case "ball":
			this.form = Entity.FORM.BALL;
			break;
		case "table":
			this.form = Entity.FORM.TABLE;
			break;
		case "pyramid":
			this.form = Entity.FORM.PYRAMID;
			break;
		case "box":
			this.form = Entity.FORM.BOX;
			break;
		}
		
		switch(size) {
		case "large":
			this.size = Entity.SIZE.LARGE;
			break;
		case "small":
			this.size = Entity.SIZE.SMALL;
			break;
		default:
			this.size = Entity.SIZE.UNDEFINED;
		}
			
		switch(color) {
		case "green":
			this.color = Entity.COLOR.GREEN;
			break;
		case "white":
			this.color = Entity.COLOR.WHITE;
			break;
		case "red":
			this.color = Entity.COLOR.RED;
			break;
		case "black":
			this.color = Entity.COLOR.BLACK;
			break;
		case "blue":
			this.color = Entity.COLOR.BLUE;
			break;
		case "yellow":
			this.color = Entity.COLOR.YELLOW;
			break;
		default:
			this.color = Entity.COLOR.UNDEFINED;
		}
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
		if (color != other.color && color != Entity.COLOR.UNDEFINED && other.color != Entity.COLOR.UNDEFINED) {
			return false;
		}
		if (form != other.form) {
			return false;
		}
		if (size != other.size && size != Entity.SIZE.UNDEFINED && other.size != Entity.SIZE.UNDEFINED) {
			return false;
		}
		return true;
	}
	
	
	
}
