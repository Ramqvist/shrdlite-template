/**
 * This is simply so that we can easily turn on / off console println's, since
 * all stdout data is fed to the Javascript client.
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
	public static void printDebug(String message) {
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
	public static void printDebug(Object object) {
		if (debug) {
			System.out.println(object.toString());
		}
	}

	/**
	 * Prints a new line to standard output if the Debug.debug variable is set
	 * to true.
	 */
	public static void printDebug() {
		if (debug) {
			System.out.println();
		}
	}

}
