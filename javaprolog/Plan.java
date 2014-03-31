import java.util.ArrayList;
import java.util.List;

/**
 *  A plan is represented by a list of Actions and a current state.
 */
public class Plan implements Comparable<Plan>{

	public final List<Action> actions;
	public State currentState;
	
	public Plan(){
		actions = new ArrayList<Action>();
	}

	public Plan(State s, List<Action> actions){
		this.actions = actions;
		this.currentState = s;
	}
	
	public void add(Action a){
		actions.add(a);
	}

	public boolean isEmpty() {
		return true;
	}

	@Override
	public int compareTo(Plan o) {
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
	
}
