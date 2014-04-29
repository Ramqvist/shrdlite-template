package src.planner;
import java.util.ArrayList;
import java.util.List;

import src.world.Entity;
import src.world.Goal;
import src.world.Relation;

/**
 *  A plan is represented by a list of Actions and a current state.
 */
public class Plan implements Comparable<Plan>{

	public final List<Action> actions;
	public State currentState;
	public Goal goal;
	public int compareCount;
	
	public Plan(){
		actions = new ArrayList<Action>();
	}

	public Plan(State s, List<Action> actions, Goal goal){
		this.actions = actions;
		this.currentState = s;
		this.goal = goal;
		compareCount = 0;
		compareCount += compareWithGoal();
		for (Relation relation : goal.getRelations()) {
			compareCount += relation.compareToWorld(currentState.world);
		}
	}
	
	public void add(Action a){
		actions.add(a);
	}

	public boolean isEmpty() {
		return true;
	}
	
	private int compareWithGoal() {
		int count = 0;
		for (List<Entity> column : currentState.world) {
			for (Entity entity : column) {
				for (Relation relation : goal.getRelations()) {
					if (!Relation.matchEntityAndRelationExact(entity, relation, currentState.world, currentState.holding).isEmpty()) {
						count++;
					}
				}
			}
		}
		return count;
	}

	@Override
	public int compareTo(Plan o) {
		if(o.actions.size() - o.compareCount > this.actions.size() - compareCount) {
			return -1;
		}
		if(o.actions.size() - o.compareCount ==  this.actions.size() - compareCount) {
			return 0;
		}
		return 1;
	}
	
	@Override
	public String toString() {
		return "(Plan " + actions + " : " + currentState + ")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((currentState == null) ? 0 : currentState.hashCode());
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
		if (!(obj instanceof Plan)) {
			return false;
		}
		Plan other = (Plan) obj;
		if (actions == null) {
			if (other.actions != null) {
				return false;
			}
		} else if (!actions.equals(other.actions)) {
			return false;
		}
		if (currentState == null) {
			if (other.currentState != null) {
				return false;
			}
		} else if (!currentState.equals(other.currentState)) {
			return false;
		}
		return true;
	}
	
	
	
}
