package src.planner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

import src.Debug;
import src.constraints.ConstraintCheck;
import src.planner.data.Action;
import src.planner.data.Plan;
import src.planner.data.PlannerException;
import src.planner.data.State;
import src.world.Entity;
import src.world.Goal;
import src.world.Relation;

public class LimitedHeuristicPlanner implements Callable<Plan> {

	private List<List<Entity>> world;
	private Entity heldEntity;
	private Goal goal;
	
	public static Integer maxDepth = Integer.MAX_VALUE;

	public LimitedHeuristicPlanner(List<List<Entity>> world, Entity heldEntity, Goal goal) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goal = goal;
	}

	public static synchronized void setMaxDepth(Integer newMaxDepth) {
		if (newMaxDepth <= maxDepth) {
			maxDepth = newMaxDepth;
		}
	}
	
	private Plan solve(Goal goal) throws PlannerException, InterruptedException {
		// We use a PriorityQueue to order all possible plans by their cost.
		LimitedPriorityQueue queue = new LimitedPriorityQueue(500);

		// TODO: We've discussed this before, but as we've currently tried to
		// solve the planner, are relations in state necessary?
		List<Relation> relations = new ArrayList<Relation>();
		State startState = new State(world, relations, heldEntity);
		queue.add(new Plan(startState, new ArrayList<Action>(), goal));
		
		if (!ConstraintCheck.isValidWorld(world)) {
			Debug.print("World is not valid!");
			Debug.print(world);
			throw new PlannerException(this + ": World is not valid!");
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
//				Debug.print(this + ": Tabu List size: " + tabuList.size());
				setMaxDepth(size);
				return plan;
			}
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			/*
			 * Fill a list of possible actions to take. We only pick the actions
			 * that make sense to try. (Hopefully!)
			 */
			List<Action> possibleActions = new ArrayList<Action>(world.size());
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
//					Debug.print(this + ": Tabu List size: " + tabuList.size());
					Debug.print(this + ": Queue size: " + queue.size());
					if (size > maxDepth) {
						throw new PlannerException(this + ": interrupted, my plan is too long: " + size + " > " + maxDepth);
					}
				}

				try {
					Plan p = new Plan(plan.currentState.takeAction(newAction), actionList, goal);
					if (newAction.command == Action.COMMAND.DROP) {
						if (ConstraintCheck.isValidColumn(p.currentState.world.get(newAction.column))) {
							queue.limitedAdd(p);
						}
					} else {
						queue.limitedAdd(p);
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
	
	private class LimitedPriorityQueue extends PriorityQueue<Plan> {
		private static final long serialVersionUID = 1L;
		private int maxSize;
		private int r = 0;
		private static final int 		GROWTH_RATE 	= 10;
		private static final boolean 	ENABLE_GROWTH	= false;

		public LimitedPriorityQueue(int maxSize) {
			super();
			this.maxSize = maxSize;
		}
		
		public void limitedAdd(Plan e) {
			if(ENABLE_GROWTH) {
				if(r++ > GROWTH_RATE) {
					r = 0;
					maxSize++;
				}
			}
			if(size() > maxSize) {
				Iterator<Plan> it = iterator();
				Plan last = it.next();
				while(it.hasNext()) {
					last = it.next();
				}
				remove(last);
			} else {
				add(e);
			}
		}
	}

	@Override
	public Plan call() throws Exception {
		try {
			long start = System.currentTimeMillis();
			Plan plan = solve(goal);
			long elapsed = System.currentTimeMillis() - start;
			Debug.print(this + ": Plan solved in: " + elapsed + " ms.");
			return plan;
		} catch (InterruptedException e) {
			return null;
		}
	}

}
