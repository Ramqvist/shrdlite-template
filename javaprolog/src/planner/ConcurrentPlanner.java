package src.planner;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

import src.Debug;
import src.constraints.ConstraintCheck;
import src.world.Entity;
import src.world.Goal;
import src.world.Relation;

public class ConcurrentPlanner implements Callable<Plan> {

	private List<List<Entity>> world;
	private Entity heldEntity;
	private Goal goal;
	
	private static Integer maxDepth = Integer.MAX_VALUE;

	public ConcurrentPlanner(List<List<Entity>> world, Entity heldEntity, Goal goal) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goal = goal;
	}

	public static synchronized void setMaxDepth(Integer newMaxDepth) {
		if (newMaxDepth <= maxDepth) {
			maxDepth = newMaxDepth;
		}
	}
	
	private Plan solve(Goal goal) throws InterruptedException {
		// We use a PriorityQueue to order all possible plans by their cost.
		PriorityQueue<Plan> queue = new PriorityQueue<>();

		// TODO: We've discussed this before, but as we've currently tried to
		// solve the planner, are relations in state necessary?
		List<Relation> relations = new ArrayList<Relation>();
		State startState = new State(world, relations, heldEntity);
		queue.add(new Plan(startState, new ArrayList<Action>(), goal));

		if (!ConstraintCheck.isValidWorld(world)) {
			Debug.print("World is not valid!");
			Debug.print(world);
			throw new InterruptedException(this + ": World is not valid!");
		}
		
		int count = 0;
		int size = 0;
		Plan plan;
		while (true) {
			count++;
			plan = queue.poll();
			if (hasReachedGoal(goal, plan.currentState)) {
				Debug.print(this + ": " + plan + " reached the goal state " + goal);
				Debug.print(this + ": Planning finished!");
				Debug.print(this + ": Actions: " + plan.actions);
				Debug.print(this + ": Total iteration count: " + count);
				setMaxDepth(size);
				return plan;
			}

			/*
			 * Fill a list of possible actions to take. We only pick the actions
			 * that make sense to try. (Hopefully!)
			 */
			List<Action> possibleActions = new ArrayList<Action>();
			for (int i = 0; i < world.size(); i++) {
				// Small optimization. No point in dropping in the same location
				// we last picked, or picking in the same location we last
				// dropped.
				if (plan.actions.size() == 0 || plan.actions.get(plan.actions.size() - 1).column != i) {
					if (plan.currentState.isHolding()) {
						possibleActions.add(new Action(Action.COMMAND.DROP, i));
					} else {
						// Big optimization. No need to try to pick something
						// from
						// an empty column.
						if (!plan.currentState.world.get(i).isEmpty()) {
							possibleActions.add(new Action(Action.COMMAND.PICK, i));
						}
					}
				}
			}

			// Take all possible actions.
			for (Action newAction : possibleActions) {
				count++;
				List<Action> actionList = new ArrayList<Action>(plan.actions.size() + 1);
				for (Action c : plan.actions) {
					actionList.add(c);
				}
				actionList.add(newAction);
				
				if (actionList.size() > size) {
					size = actionList.size();
					Debug.print(this + ": " + size);
					if (size > maxDepth) {
						throw new InterruptedException(this + ": interrupted, my plan is too long: " + size + " > " + maxDepth);
					}
				}

				try {
					Plan p = new Plan(plan.currentState.takeAction(newAction), actionList, goal);

					if (newAction.command == Action.COMMAND.DROP) {
						if (ConstraintCheck.isValidColumn(p.currentState.world.get(newAction.column))) {
							queue.add(p);
						}
					} else {
						queue.add(p);
					}
				} catch (Exception e) {
					Debug.print(e);
					// The action was rejected, so we do nothing.
				}
			}
		}
	}

	private boolean hasReachedGoal(Goal goal, State state) {
		int count = 0;
		for (Relation relation : goal.getRelations()) {
			if (relation.getType() == Relation.TYPE.HELD) {
				if (state.holding != null && state.holding.equalsExact(relation.getEntityA())) {
					count++;
					break;
				}
			}
		}
		
		for (List<Entity> column : state.world) {
			for (Entity entity : column) {
				for (Relation relation : goal.getRelations()) {
					if (!Relation.matchEntityAndRelationExact(entity, relation, state.world, state.holding).isEmpty()) {
						count++;
					}
				}
			}
		}
		
		return count == goal.getRelations().size();
	}

	@Override
	public Plan call() throws Exception {
		long start = System.currentTimeMillis();
		Plan plan = solve(goal);		
		long elapsed = System.currentTimeMillis() - start;
		Debug.print(this + ": Plan solved in: " + elapsed + " ms.");
		return plan;
	}

}
