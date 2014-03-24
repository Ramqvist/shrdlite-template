/**
 * An Entity. 
 *
 */
public class Entity {
	
	public enum FORM {
	    ON_TOP_OF, ABOVE, UNDER, BESIDE, LEFT_OF, RIGHT_OF
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
	

}
