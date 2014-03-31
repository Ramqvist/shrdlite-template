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

	public String toString() {
		if (command == COMMAND.PICK) {
			return "pick " + column;
		} else {
			return "drop " + column;
		}

	}
}
