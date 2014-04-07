import java.util.ArrayList;
import java.util.List;

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
					if (!Relation.matchEntityAndRelationExact(entity, relation, currentState.world).isEmpty()) {
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
	
}
