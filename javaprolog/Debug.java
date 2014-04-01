/**
 * This is simply so that we can easily turn on / off console println's, since
 * all standard output is fed to the JavaScript client.
 * 
 */
public class Debug {

	public static boolean debug = false;

	/**
	 * Prints the given message to standard output if the Debug.debug variable
	 * is set to true.
	 * 
	 * @param message
	 *            The String message to be printed.
	 */
	public static void print(String message) {
		if (debug) {
			System.out.println(message);
		}
	}

	/**
	 * Prints the toString() representation of the given object to standard
	 * output if the Debug.debug variable is set to true.
	 * 
	 * @param object
	 *            The object to be printed.
	 */
	public static void print(Object object) {
		if (debug) {
			System.out.println(object.toString());
		}
	}

	/**
	 * Prints a new line to standard output if the Debug.debug variable is set
	 * to true.
	 */
	public static void print() {
		if (debug) {
			System.out.println();
		}
	}

}
