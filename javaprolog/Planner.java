import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Planner {
	List<List<Entity>> world;
	private Goal goal;

	public Planner(List<List<Entity>> world) {
		this.world = world;
	}

	public Plan solve(Goal goal) {
		this.goal = goal;
		PriorityQueue<Plan> queue = new PriorityQueue<>();
		
		List<Relation> relations = new ArrayList<Relation>(); //TODO: FIX
		State startState = new State(world, relations);
		queue.add(new Plan(startState, new ArrayList<Action>()));
		
		boolean reachedGoal = false;
		Plan goalPlan = null;
		while(!reachedGoal) {
			Plan plan = queue.poll();
			reachedGoal = hasReachedGoal(goal, plan.currentState);
			if(reachedGoal) {
				goalPlan = plan;
			}
			List<Action> possibleActions = new ArrayList<Action>();
			for(int i = 0; i < world.size() ; i++) {
				if(plan.currentState.isHolding())  {
					possibleActions.add(new Action(Action.COMMAND.DROP, i));
				} else {
					possibleActions.add(new Action(Action.COMMAND.PICK, i));
				}
			}
			for(Action newAction : possibleActions) {
				List<Action> actionList = new ArrayList<Action>();
				for(Action c : plan.actions) {
					actionList.add(c);
				}
				actionList.add(newAction);
				Plan p = new Plan(plan.currentState.takeAction(newAction), actionList);
				if (ConstraintCheck.isValidWorld(p.currentState.world)) {
					queue.add(p);
				}
			}
		}
		return goalPlan;
	}
	
	private static boolean hasReachedGoal(Goal g, State s) {
		for(Relation r : g.getRelations()) {
			if(!s.exist(r)) {
				return false;
			}
		}
		return true;
	}

}
