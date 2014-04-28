package tests;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import src.constraints.ConstraintCheck;
import src.world.Entity;

/**
 * Tests {@link ConstraintCheck#isValidColumn(List)} if it correct.
 * 
 * @author Erik
 *
 */
public class ConstraintCheckTest {
	
	
	/**
	 * Balls must be in boxes or on the floor, otherwise they roll away.
	 */
	@Test
	public void testBallInBox() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.PLANK, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));

		if (ConstraintCheck.isValidColumn(entityList)) {
			entityList = new ArrayList<Entity>();
			entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			if (!ConstraintCheck.isValidColumn(entityList)) {
				entityList = new ArrayList<Entity>();
				entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
				entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
				assertTrue(ConstraintCheck.isValidColumn(entityList));
			} else {
				fail("Test testBoxOnlySupportedbyTablesPlanks() Failed 2");
			}
		} else {
			fail("Test testBoxOnlySupportedbyTablesPlanks() Failed 1");
		}
	}

	
	//Balls cannot support anything
	@Test
	public void testBallCannotSupport() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));

		if (!ConstraintCheck.isValidColumn(entityList)) {
			entityList = new ArrayList<Entity>();
			entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			assertTrue(ConstraintCheck.isValidColumn(entityList));
		} else {
			fail("Test testBallCannotSupport() Failed");
		}
	}
	
	// Small objects cannot support large objects.
	@Test
	public void testSmallCannotSupportLarge() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.PYRAMID, Entity.SIZE.SMALL, Entity.COLOR.BLACK));

		if (ConstraintCheck.isValidColumn(entityList)) {
			
			entityList = new ArrayList<Entity>();
			entityList.add(new Entity(Entity.FORM.PLANK, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			assertTrue(!ConstraintCheck.isValidColumn(entityList));
		} else {
			fail("Test testSmallCannotSupportLarge() Failed");
		}
	}
	

	//Boxes cannot contain pyramids or planks of the same size. 
	@Test
	public void testBoxCannotContainPyrPlank() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.PYRAMID, Entity.SIZE.LARGE, Entity.COLOR.BLACK));

		if (!ConstraintCheck.isValidColumn(entityList)) {
			entityList = new ArrayList<Entity>();
			entityList.add(new Entity(Entity.FORM.PLANK, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.PLANK, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
			assertTrue(ConstraintCheck.isValidColumn(entityList));
		} else {
			fail("Test testBoxCannotContainPyrPlank() Failed");
		}
	}
	
	/**
	 * Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.
	 */
	@Test
	public void testBoxOnlySupportedbyTablesPlanks() {
		List<Entity> entityList = new ArrayList<Entity>();
		entityList.add(new Entity(Entity.FORM.PLANK, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
		entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.LARGE, Entity.COLOR.BLACK));

		if (!ConstraintCheck.isValidColumn(entityList)) {
			entityList = new ArrayList<Entity>();
			entityList.add(new Entity(Entity.FORM.PLANK, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BRICK, Entity.SIZE.LARGE, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BOX, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
			entityList.add(new Entity(Entity.FORM.BALL, Entity.SIZE.SMALL, Entity.COLOR.BLACK));
			assertTrue(ConstraintCheck.isValidColumn(entityList));
		} else {
			fail("Test testBoxOnlySupportedbyTablesPlanks() Failed 1");
		}
	}
	
}
