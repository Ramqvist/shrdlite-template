public class Action {

	public enum COMMAND {
		PICK, DROP
	}

	public final COMMAND command;
	public final int column;

	public Action(COMMAND com, int col) {
		command = com;
		column = col;
	}
<<<<<<< HEAD
	
=======

>>>>>>> 77edfe7da964b5d5db6a135f45fad4115f498034
	public String toString() {
		if (command == COMMAND.PICK) {
			return "pick " + column;
		} else {
			return "drop " + column;
		}

	}
}
