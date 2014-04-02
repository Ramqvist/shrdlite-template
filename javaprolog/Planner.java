import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Planner {

	List<List<Entity>> world;

	public Planner(List<List<Entity>> world) {
		this.world = world;
	}

	public Plan solve(Goal goal) {
		// We use a PriorityQueue to order all possible plans by their cost.
		PriorityQueue<Plan> queue = new PriorityQueue<>();

		// TODO: We've discussed this before, but as we've currently tried to
		// solve the planner, are relations in state necessary?
		List<Relation> relations = new ArrayList<Relation>();
		State startState = new State(world, relations);
		queue.add(new Plan(startState, new ArrayList<Action>()));

		if (!ConstraintCheck.isValidWorld(world)) {
			Debug.print("World is not valid!");
			return null;
		}
		
		boolean reachedGoal = false;
		Plan goalPlan = null;
		// Hey I didn't even know Java HAD labels!
		while (true) {
			Plan plan = queue.poll();
			reachedGoal = hasReachedGoal(goal, plan.currentState);
			if (reachedGoal) {
				Debug.print(plan + " reached the goal state " + goal);
				goalPlan = plan;
				break;
			}

			List<Action> possibleActions = new ArrayList<Action>();
			for (int i = 0; i < world.size(); i++) {
				if (plan.currentState.isHolding()) {
					possibleActions.add(new Action(Action.COMMAND.DROP, i));
				} else {
					// Small optimization. No need to try to pick something from
					// an empty column.
					if (!plan.currentState.world.get(i).isEmpty())
						possibleActions.add(new Action(Action.COMMAND.PICK, i));
				}
			}

			for (Action newAction : possibleActions) {
				List<Action> actionList = new ArrayList<Action>();
				for (Action c : plan.actions) {
					actionList.add(c);
				}
				actionList.add(newAction);
				
				try {
					Plan p = new Plan(plan.currentState.takeAction(newAction), actionList);
					
					if (newAction.command == Action.COMMAND.DROP) {
						if (ConstraintCheck.isValidColumn(p.currentState.world.get(newAction.column))) { 
							queue.add(p);
						}
					} else {
						queue.add(p);
					}
					
					if (hasReachedGoal(goal, p.currentState)) {
						Debug.print(p.actions);
						return p;
					}
				} catch (Exception e) {
					Debug.print(e);
					// The action was rejected, so we do nothing.
				}
			}
		}
//		Debug.print(queue);
		return goalPlan;
	}
	
	private static boolean hasReachedGoal(Goal goal, State state) {
		int count = 0;
		for (List<Entity> column : state.world) {
			for (Entity entity : column) {
				for (Relation relation : goal.getRelations()) {
					if (!Relation.matchEntityAndRelationExact(entity, relation, state.world).isEmpty()) {
						count++;
					}
				}
			}
		}
		return count == goal.getRelations().size();
	}

}
