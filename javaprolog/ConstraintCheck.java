import java.util.ArrayList;
import java.util.List;

/**
 * Class for checking constraints.
 * <p>
 * Source: http://www.cse.chalmers.se/edu/course/TIN172/project.html
 * The constraints are as follows:
 * 
 * The world is ruled by physical laws that constrain the placement and movement of the objects:

    The floor can support any number of objects. 	(SKIP)
    All objects must be supported by something. 	(SKIP)
    The arm can only hold one object at the time. 	(SKIP)
    The arm can only pick up free objects. 			(SKIP)
    Objects are “in” boxes, but “on” other objects.
    Balls must be in boxes or on the floor, otherwise they roll away.
    Balls cannot support anything.
    Small objects cannot support large objects.
    Boxes cannot contain pyramids or planks of the same size. 
    Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.
 */
public class ConstraintCheck {
	
	/**
	 * Method to test ConstraintCheck.
	 * TODO: Make JUNIT Test instead!
	 */
	public static void main(String[] args) {
		
		//TEST BALL CANNOT SUPPORT
		testBallCannotSupport();
	}
	
	private static void testBallCannotSupport() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		
		if(!isValidColumn(entityList)) {
			System.out.println("Yakshemaish! GREAT SUCCESS!!!");
		} else {
			System.err.println("TEST BALL CANNOT SUPPORT FAILED");
		}

		entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		
		if(isValidColumn(entityList)) {
			System.out.println("Yakshemaish! GREAT SUCCESS!!!");
		} else {
			System.err.println("TEST BALL CANNOT SUPPORT FAILED");
		}
	}

	/**
	 * @return Checks if the given Column represented as a List of Entities is valid under the constraints. 
	 */
	public static boolean isValidColumn(List<Entity> entityList) {
		if(entityList == null) {
			System.err.println("NullPointer in isValidColumn!");
			return false;
		}
		if(entityList.isEmpty()) {
			return true;
		}
		//If Any ball is NOT the last Entity, then the Column is not Valid!
		for (int i = 0; i < entityList.size(); i++) {
			Entity e = entityList.get(i);
			if(e.getForm() == Entity.FORM.BALL && i+1 != entityList.size()) {
				return false;
			}
		}
		//Small objects cannot support large objects.
		boolean previousIsSmall = false;
		for (int i = 0; i < entityList.size(); i++) {
			Entity e = entityList.get(i);
			if(e.getSize() == Entity.SIZE.LARGE && previousIsSmall) {
				return false;
			}
			previousIsSmall = e.getSize() == Entity.SIZE.SMALL;
		}
		
		return true;
	}

	/**
	 * @return Checks if the Relations are valid under the constraints. 
	 */
	public static boolean isValidRelations(List<Relation> relations) {
		if(relations == null) {
			System.err.println("NullPointer in isValidRelations!");
			return false;
		}
		if(relations.isEmpty()) {
			return true;
		}
		return true;
	}
}
