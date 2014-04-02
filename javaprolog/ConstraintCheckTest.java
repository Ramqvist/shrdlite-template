import java.util.ArrayList;
import java.util.List;


public class ConstraintCheckTest {
	
	/**
	 * Method to test ConstraintCheck. TODO: Make JUNIT Test instead!
	 */
	public static void main(String[] args) {

		// TEST BALL CANNOT SUPPORT
//		testBallCannotSupport();
//		testSmallCannotSupportLarge();
		testBallCannotSupport();
	}

	private static void testBallCannotSupport() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));

		if (!ConstraintCheck.isValidColumn(entityList)) {
			entityList = new ArrayList<Entity>();
			entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			if (ConstraintCheck.isValidColumn(entityList)) {
				System.out.println("Test testBallCannotSupport() Passed");
			} else {
				System.err.println("Test testBallCannotSupport() Failed");
			}
		} else {
			System.err.println("Test testBallCannotSupport() Failed");
		}
	}
	
	// Small objects cannot support large objects.
	private static void testSmallCannotSupportLarge() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.PYRAMID, Entity.SIZE.SMALL, Entity.COLOR.BLACK));

		if (ConstraintCheck.isValidColumn(entityList)) {
			
			entityList = new ArrayList<Entity>();
			entityList.add(new Entity(Entity.FORM.PLANK, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			
			if (!ConstraintCheck.isValidColumn(entityList)) {
				System.out.println("Test testSmallCannotSupportLarge() Passed");
			} else {
				System.err.println("Test testSmallCannotSupportLarge() Failed");
			}
		} else {
			System.err.println("Test testSmallCannotSupportLarge() Failed");
		}
	}
	
}
