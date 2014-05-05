package src.planner.data;
import java.util.ArrayList;
import java.util.List;

import src.world.Entity;
import src.world.Relation;

/**
 * Representation of world state. Could be useful to have both array and
 * relation representations in the same object.
 */

public class State {
	
	public List<List<Entity>> world;
	public List<Relation> relations;
	public Entity holding = null;

	public State(List<List<Entity>> world, List<Relation> relations, Entity heldEntity) {
		this.world = world;
		this.relations = relations;
		this.holding = heldEntity;
	}

	/**
	 * Clone constructor.
	 */
	public State(State state) {
		this.relations = new ArrayList<Relation>();
		for (Relation r : state.relations) {
			this.relations.add(r.copy());
		}
		this.world = new ArrayList<List<Entity>>(state.world.size());
		for (List<Entity> entityList : state.world) {
			List<Entity> l = new ArrayList<Entity>(entityList.size());
			for (Entity ent : entityList) {
				l.add(ent);
			}
			world.add(l);
		}
		if (state.holding != null) {
			this.holding = state.holding;
		}
	}

	/**
	 * Checks if the state has the given relation.
	 */
	public boolean exist(Relation r) {
		return relations.contains(r);
	}

	/**
	 * Performs the given action in the world that this state is in. If the
	 * given action cannot be performed, an exception is thrown. If the given
	 * action was OK, the new state of the world after the action has been
	 * performed is returned. This state remains unchanged.
	 * 
	 * @param a
	 *            The action to be performed.
	 * @return A new state representing the world after the action has been
	 *         performed
	 * @throws NullPointerException
	 *             If the given action says to drop an object but the arm is not
	 *             holding anything.
	 * @throws IllegalStateException
	 *             If the given action says to pick an object when the arm
	 *             already holds an object / doesn't have any object to pick up
	 *             in the given column of the action.
	 */
	public State takeAction(Action a) throws NullPointerException, IllegalStateException {
		State s = new State(this);
		if (a.command == Action.COMMAND.DROP) {
			if (s.holding == null) {
				throw new NullPointerException("Not holding anything.");
			}
			s.world.get(a.column).add(s.holding);
			s.holding = null;
		} else {
			if (s.holding != null) {
				throw new IllegalStateException("Already holding a object.");
			}
			if (s.world.get(a.column).isEmpty()) {
				throw new IllegalStateException("No object to pick up.");
			}
			s.holding = s.world.get(a.column).remove(s.world.get(a.column).size() - 1);
		}
		return s;
	}

	public boolean isHolding() {
		return holding != null;
	}

	@Override
	public String toString() {
		return "(State " + world + " : " + relations + " : HOLDING " + holding + ")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((holding == null) ? 0 : holding.hashCode());
		result = prime * result + ((relations == null) ? 0 : relations.hashCode());
//		result = prime * result + ((world == null) ? 0 : world.hashCode());
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
		if (!(obj instanceof State)) {
			return false;
		}
		State other = (State) obj;
		if (holding == null) {
			if (other.holding != null) {
				return false;
			}
		} else if (!holding.equals(other.holding)) {
			return false;
		}
		if (relations == null) {
			if (other.relations != null) {
				return false;
			}
		} else if (!relations.equals(other.relations)) {
			return false;
		}
		if (world == null) {
			if (other.world != null) {
				return false;
			}
		} else if (!world.equals(other.world)) {
			return false;
		}
		return true;
	}
	
	

}
