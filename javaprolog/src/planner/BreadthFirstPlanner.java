package src.planner;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import src.Debug;
import src.constraints.ConstraintCheck;
import src.planner.data.Action;
import src.planner.data.SimplePlan;
import src.planner.data.State;
import src.world.Entity;
import src.world.Goal;
import src.world.Relation;

public class BreadthFirstPlanner {

	List<List<Entity>> world;
	Entity heldEntity;

	public BreadthFirstPlanner(List<List<Entity>> world, Entity heldEntity) {
		this.world = world;
		this.heldEntity = heldEntity;
	}

	public SimplePlan solve(Goal goal, int maxDepth) {
		// We use a PriorityQueue to order all possible plans by their cost.
		PriorityQueue<SimplePlan> queue = new PriorityQueue<>();

		// TODO: We've discussed this before, but as we've currently tried to
		// solve the planner, are relations in state necessary?
		List<Relation> relations = new ArrayList<Relation>();
		State startState = new State(world, relations, heldEntity);
		queue.add(new SimplePlan(startState, new ArrayList<Action>(), goal));

		if (!ConstraintCheck.isValidWorld(world)) {
			Debug.print("World is not valid!");
			Debug.print(world);
			return null;
		}
		int count = 0;
		int size = 0;
		boolean reachedGoal = false;
		SimplePlan goalPlan = null;
		while (true) {
			if (Thread.interrupted()) {
				try {
					throw new InterruptedException();
				} catch (InterruptedException e1) {
				}
				return null;
			}
			count++;
			SimplePlan plan = queue.poll();
			reachedGoal = hasReachedGoal(goal, plan.currentState);
			if (reachedGoal) {
				Debug.print(plan + " reached the goal state " + goal);
				goalPlan = plan;
				break;
			}

			/*
			 * Optimization to add:
			 * 
			 * Check for cycles.
			 * 
			 * Find the object to move, prefer to pick from that column?
			 */

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
					Debug.print(size);
				}				
//				Debug.print(actionList);
				if (actionList.size() >= maxDepth) {
					return null; // TODO Make nicer?
				}

				try {
					SimplePlan p = new SimplePlan(plan.currentState.takeAction(newAction), actionList, goal);

					if (newAction.command == Action.COMMAND.DROP) {
						if (ConstraintCheck.isValidColumn(p.currentState.world.get(newAction.column))) {
							queue.add(p);
						} else {
							// Ugly hack. We do this because if the plan was rejected we don't want to check if it has reached the goal.
							p = null; 
						}
					} else {
						queue.add(p);
					}

					// p is set to null if it is rejected by the constraints.
					if (p != null && hasReachedGoal(goal, p.currentState)) {
						Debug.print(p.actions);
						Debug.print(count);
						return p;
					}
				} catch (RuntimeException e) {
					Debug.print(e);
					// The action was rejected, so we do nothing.
				}
			}
		}
		return goalPlan;
	}

	private static boolean hasReachedGoal(Goal goal, State state) {
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

}
