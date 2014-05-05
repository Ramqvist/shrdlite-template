package src.planner.data;
import java.util.ArrayList;
import java.util.List;

import src.world.Goal;

/**
 * Simple Plan to not use heuristics to compare with other objects.
 * <p>
 * A plan is represented by a list of Actions and a current state.
 */
public class SimplePlan implements IPlan, Comparable<SimplePlan>{

	public final List<Action> actions;
	public State currentState;
	public Goal goal;
	
	public SimplePlan(){
		actions = new ArrayList<Action>();
	}

	public SimplePlan(State s, List<Action> actions, Goal goal){
		this.actions = actions;
		this.currentState = s;
		this.goal = goal;
	}
	
	public void add(Action a){
		actions.add(a);
	}

	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public int compareTo(SimplePlan o) {
		if(o.actions.size() > this.actions.size()) {
			return -1;
		}
		if(o.actions.size() ==  this.actions.size()) {
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
		if (!(obj instanceof SimplePlan)) {
			return false;
		}
		SimplePlan other = (SimplePlan) obj;
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

	@Override
	public List<Action> getActions() {
		return actions;
	}
	
	
	
}
