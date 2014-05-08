package src;
/**
 * This is simply so that we can easily turn on / off console println's, since
 * all standard output is fed to the JavaScript client.
 * 
 */
public class Debug {
<<<<<<< HEAD

	public static final boolean debug = true;
	public static final boolean benchmark = true;
=======
	
	public static final boolean debug = false;
	public static final boolean benchmark = false;
>>>>>>> ecd19b797f02fde5d283d8164b465c32802f7f7b

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
			System.out.println(object == null ? "null" : object.toString());
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
