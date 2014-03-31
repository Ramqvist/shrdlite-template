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

		boolean reachedGoal = false;
		Plan goalPlan = null;
//		int count = 0; // Used to count iterations.
		while (true) {
			Plan plan = queue.poll();
//			count++;
			reachedGoal = hasReachedGoal(goal, plan.currentState);
			if (reachedGoal) {
				System.out.println(plan + " reached the goal state " + goal);
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
//				count++;
				List<Action> actionList = new ArrayList<Action>();
				for (Action c : plan.actions) {
					actionList.add(c);
				}
				actionList.add(newAction);
//				System.out.println(actionList);
				try {
					Plan p = new Plan(plan.currentState.takeAction(newAction), actionList);
					if (ConstraintCheck.isValidWorld(p.currentState.world)) {
						queue.add(p);
					}
				} catch (Exception e) {
					System.out.println(e);
					// The action was rejected, so we do nothing.
				}
			}
		}
//		System.out.println(count);
		return goalPlan;
	}

	private static boolean hasReachedGoal(Goal goal, State state) {
		int count = 0;
		for (List<Entity> column : state.world) {
			for (Entity entity : column) {
				for (Relation relation : goal.getRelations()) {
					if (!Relation.matchEntityAndRelationExact(entity, relation, state.world).isEmpty())
						count++;
				}
			}
		}
		return count == goal.getRelations().size();
	}

	// private static boolean hasReachedGoal(Goal g, State s) {
	// for(Relation r : g.getRelations()) {
	// if(!s.exist(r)) {
	// return false;
	// }
	// }
	// return true;
	// }

}
