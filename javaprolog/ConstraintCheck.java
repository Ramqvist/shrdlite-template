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
    Objects are “in” boxes, but “on” other objects. (SKIP)
    Balls must be in boxes or on the floor, otherwise they roll away.
    Balls cannot support anything.
    Small objects cannot support large objects.
    Boxes cannot contain pyramids or planks of the same size. 
    Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.
 */
public class ConstraintCheck {

	/**
	 * @return Checks if the given Column represented as a List of Entities is
	 *         valid under the constraints.
	 */
	public static boolean isValidColumn(List<Entity> entityList) {
		if (entityList == null) {
			System.err.println("NullPointer in isValidColumn!");
			return false;
		}
		if (entityList.isEmpty()) {
			return true;
		}
		boolean previousIsSmall = false;
		for (int i = 0; i < entityList.size(); i++) {
			//Balls must be in boxes or on the floor, otherwise they roll away.
			Entity e = entityList.get(i);
			if (e.getForm() == Entity.FORM.BALL && i != 0 && entityList.get(i-1).getForm() != Entity.FORM.BOX) {
				return false;
			}

			// If Any ball is NOT the last Entity, then the Column is not Valid!
			// (Balls cannot support anything)
			if (e.getForm() == Entity.FORM.BALL && i + 1 != entityList.size()) {
				return false;
			}

			// Small objects cannot support large objects.
			if (e.getSize() == Entity.SIZE.LARGE && previousIsSmall) {
				return false;
			}
			previousIsSmall = e.getSize() == Entity.SIZE.SMALL;

			//Boxes cannot contain pyramids or planks of the same size. 
			if(e.getForm() == Entity.FORM.BOX && i != entityList.size() - 1) {
				Entity next = entityList.get(i+1);
				if(next.getForm() == Entity.FORM.PYRAMID || next.getForm() == Entity.FORM.PLANK) {
					if(e.getSize() == Entity.SIZE.LARGE && next.getSize() == Entity.SIZE.LARGE) {
						return false;
					}
					if(e.getSize() == Entity.SIZE.SMALL) {
						return false;
					}
				}
			}
			
			//Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.
			if(i != 0 && e.getForm() == Entity.FORM.BOX) {
				Entity previous = entityList.get(i-1);
				if (previous.getForm() == Entity.FORM.TABLE || previous.getForm() == Entity.FORM.PLANK) {
					//..of the same size
					if(e.getSize() == Entity.SIZE.LARGE && previous.getSize() == Entity.SIZE.SMALL) {
						return false;
					}
				} else if (previous.getForm() == Entity.FORM.BRICK) {
					if(previous.getSize() == Entity.SIZE.SMALL) {
						return false;
					}
				} else {
					//Boxes can only be supported by tables or planks.
					return false;
				}
			}
		}
		return true;
	}
	

	/**
	 * The original isValidColumn without merged loops.
	 */
	public static boolean isValidColumnOriginal(List<Entity> entityList) {
		if (entityList == null) {
			System.err.println("NullPointer in isValidColumn!");
			return false;
		}
		if (entityList.isEmpty()) {
			return true;
		}
		//TODO: Merge for loops.
		
		//Balls must be in boxes or on the floor, otherwise they roll away.
		for (int i = 0; i < entityList.size(); i++) {
			Entity e = entityList.get(i);
			if (e.getForm() == Entity.FORM.BALL && i != 0 && entityList.get(i-1).getForm() != Entity.FORM.BOX) {
				return false;
			}
		}
		
		// If Any ball is NOT the last Entity, then the Column is not Valid!
		// (Balls cannot support anything)
		for (int i = 0; i < entityList.size(); i++) {
			Entity e = entityList.get(i);
			if (e.getForm() == Entity.FORM.BALL && i + 1 != entityList.size()) {
				return false;
			}
		}
		
		// Small objects cannot support large objects.
		boolean previousIsSmall = false;
		for (int i = 0; i < entityList.size(); i++) {
			Entity e = entityList.get(i);
			if (e.getSize() == Entity.SIZE.LARGE && previousIsSmall) {
				return false;
			}
			previousIsSmall = e.getSize() == Entity.SIZE.SMALL;
		}
		
		//Boxes cannot contain pyramids or planks of the same size. 
		for (int i = 0; i < entityList.size(); i++) {
			Entity e = entityList.get(i);
			if(e.getForm() == Entity.FORM.BOX && i != entityList.size() - 1) {
				Entity next = entityList.get(i+1);
				if(next.getForm() == Entity.FORM.PYRAMID || next.getForm() == Entity.FORM.PLANK) {
					if(e.getSize() == Entity.SIZE.LARGE && next.getSize() == Entity.SIZE.LARGE) {
						return false;
					}
					if(e.getSize() == Entity.SIZE.SMALL) {
						return false;
					}
				}
			}
		}
		
		//Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.
		for (int i = 0; i < entityList.size(); i++) {
			Entity e = entityList.get(i);
			if(i != 0 && e.getForm() == Entity.FORM.BOX) {
				Entity previous = entityList.get(i-1);
				if (previous.getForm() == Entity.FORM.TABLE || previous.getForm() == Entity.FORM.PLANK) {
					//..of the same size
					if(e.getSize() == Entity.SIZE.LARGE && previous.getSize() == Entity.SIZE.SMALL) {
						return false;
					}
				} else if (previous.getForm() == Entity.FORM.BRICK) {
					if(previous.getSize() == Entity.SIZE.SMALL) {
						return false;
					}
				} else {
					//Boxes can only be supported by tables or planks.
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * @return Checks if the Relations are valid under the constraints.
	 */
	public static boolean isValidRelations(List<Relation> relations) {
		if (relations == null) {
			System.err.println("NullPointer in isValidRelations!");
			return false;
		}
		if (relations.isEmpty()) {
			return true;
		}
		for (Relation relation : relations) {
			if (relation.getType().equals(Relation.TYPE.INSIDE)) {
				/*
				 * Check the INSIDE relation type.
				 */
				
				// Balls must be inside boxes. (On the floor handled below.)
				if (relation.getEntityA().getForm() == Entity.FORM.BALL && relation.getEntityB().getForm() != Entity.FORM.BOX) {
					return false;
				}
				
				// Objects are only inside boxes.
				if (relation.getEntityB().getForm() != Entity.FORM.BOX) {
					return false;
				}
				
				// Small boxes can only contain small items.
				if (relation.getEntityB().getSize() == Entity.SIZE.SMALL) {
					if (relation.getEntityA().getSize() != Entity.SIZE.SMALL) {
						return false;
					}
				}
				
				if (relation.getEntityA().getForm() == Entity.FORM.PLANK || relation.getEntityA().getForm() == Entity.FORM.PYRAMID || relation.getEntityA().getForm() == Entity.FORM.BOX) {
					if (relation.getEntityB().getSize() == Entity.SIZE.LARGE) {
						// Boxes cannot contain planks or pyramids of the same size
						// as the box.
						if (relation.getEntityA().getSize() != Entity.SIZE.SMALL) {
							return false;
						}
					} else {
						return false;
					}
				}
			} else if (relation.getType() == Relation.TYPE.ON_TOP_OF) {
				/*
				 * Checking the ON_TOP_OF relations. 
				 */
				
				// Balls cannot support anything.
				if (relation.getEntityB().getForm() == Entity.FORM.BALL) {
					return false;
				}

				// Small objects cannot support large objects.
				if (relation.getEntityA().getSize() == Entity.SIZE.LARGE && relation.getEntityB().getSize() == Entity.SIZE.SMALL) {
					return false;
				}
				
				// Boxes can only be supported by tables or planks of the same size, but large boxes can also be supported by large bricks.				
				if (relation.getEntityA().getForm() == Entity.FORM.BOX) {
					if (relation.getEntityB().getForm() != Entity.FORM.FLOOR) {
						if (relation.getEntityA().getSize() == Entity.SIZE.LARGE) {
							if (relation.getEntityB().getSize() == Entity.SIZE.LARGE) {
								if (relation.getEntityB().getForm() != Entity.FORM.BRICK && relation.getEntityB().getForm() != Entity.FORM.TABLE && relation.getEntityB().getForm() != Entity.FORM.PLANK) {
									Debug.print("fail fail fail");
									return false;
								}
							}
						} else {
							// If the box is small it cannot be supported by a small brick.
							if (relation.getEntityB().getSize() == Entity.SIZE.SMALL && relation.getEntityB().getForm() == Entity.FORM.BRICK) {
								return false;
							}
							
							if (relation.getEntityB().getForm() != Entity.FORM.BRICK && relation.getEntityB().getForm() != Entity.FORM.TABLE && relation.getEntityB().getForm() != Entity.FORM.PLANK) {
								return false;
							}
						}
					}
				}
				
				// Balls can only be on top of the floor or inside a box
				if (relation.getEntityA().getForm() == Entity.FORM.BALL && relation.getType() == Relation.TYPE.ON_TOP_OF) {
					if (relation.getEntityB().getForm() != Entity.FORM.FLOOR) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static boolean isValidWorld(List<List<Entity>> world) {
		for(List<Entity> column : world) {
			if(!isValidColumn(column)) {
				return false;
			}
		}
		return true;
	}
}
