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
	
}
