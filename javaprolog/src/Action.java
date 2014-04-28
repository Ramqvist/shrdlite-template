package src;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + ((command == null) ? 0 : command.hashCode());
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
		if (!(obj instanceof Action)) {
			return false;
		}
		Action other = (Action) obj;
		if (column != other.column) {
			return false;
		}
		if (command != other.command) {
			return false;
		}
		return true;
	}
	
	
}
