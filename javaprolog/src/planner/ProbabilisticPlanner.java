package src.planner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import src.Debug;
import src.constraints.ConstraintCheck;
import src.planner.data.Action;
import src.planner.data.SimplePlan;
import src.planner.data.State;
import src.world.Entity;
import src.world.Goal;
import src.world.Relation;

/**
 * Planner that takes steps based on the probabilities.
 * 
 * http://www2.informatik.uni-freiburg.de/~ki/teaching/ss05/aip/s05.pdf
 * 
 * @author Erik
 *
 */
public class ProbabilisticPlanner implements Callable<SimplePlan> {

	private List<List<Entity>> world;
	private Entity heldEntity;
	private Goal goal;
	private int count = 0;
	
	private static Integer maxDepth = Integer.MAX_VALUE;
	private Random ran = new Random();	

	public ProbabilisticPlanner(List<List<Entity>> world, Entity heldEntity, Goal goal) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goal = goal;
	}

	public static synchronized void setMaxDepth(Integer newMaxDepth) {
		if (newMaxDepth <= maxDepth) {
			maxDepth = newMaxDepth;
		}
	}
	
	private int getProbability(List<Relation> relations, List<List<Entity>> world, Entity holding) {
		int compareCount = 0;
		compareCount += compareWithGoal(world, holding);
		for (Relation relation : goal.getRelations()) {
			compareCount += relation.compareToWorld(world);
		}
		return compareCount;
	}
	
	private int compareWithGoal(List<List<Entity>> world, Entity holding) {
		int count = 0;
		for (List<Entity> column : world) {
			for (Entity entity : column) {
				for (Relation relation : goal.getRelations()) {
					if (!Relation.matchEntityAndRelationExact(entity, relation, world, holding).isEmpty()) {
						count++;
					}
				}
			}
		}
		return count;
	}
	
	private SimplePlan solve(Goal goal) throws InterruptedException {
//		Debug.print("Started Planning using Gibbs!");
		List<Relation> relations = new ArrayList<Relation>();
		State startState = new State(world, relations, heldEntity);

		if (!ConstraintCheck.isValidWorld(world)) {
			Debug.print("World is not valid!");
			Debug.print(world);
			throw new InterruptedException(this + ": World is not valid!");
		}
		
		int size = 0;
		SimplePlan plan = new SimplePlan(startState, new ArrayList<Action>(), goal);
		while (true) {
			count++;
//			plan = queue.poll();
			if (hasReachedGoal(goal, plan.currentState)) {
				Debug.print(this + ": " + plan + " reached the goal state " + goal);
				Debug.print(this + ": Planning finished!");
				Debug.print(this + ": Actions: " + plan.actions);
				Debug.print(this + ": Total iteration count: " + count);
				return plan;
			}

			/*
			 * Fill a list of possible actions to take. We only pick the actions
			 * that make sense to try. (Hopefully!)
			 */
			// Small optimization. No point in dropping in the same location
			// we last picked, or picking in the same location we last
			// dropped.

			List<Action> possibleActions = new ArrayList<Action>();
			for(int i = 0 ; i < world.size() ; i++) {
				if (plan.currentState.isHolding()) {
					possibleActions.add(new Action(Action.COMMAND.DROP, i));
				} else {
					// Big optimization. No need to try to pick something
					// from
					// an empty column.
					possibleActions.add(new Action(Action.COMMAND.PICK, i));
				}
			}

			// Take all possible actions.
//			count++;
			
			List<Action> actionList = new ArrayList<Action>(plan.actions.size() + 1);
			for (Action c : plan.actions) {
				actionList.add(c);
			}
			
			if (plan.actions.size() > maxDepth) {
//				return null;
				size = plan.actions.size();
//					Debug.print(this + ": " + size);
//				if (size > maxDepth) {
					throw new InterruptedException(this + ": interrupted, my plan is too long: " + size + " > " + maxDepth);
//				}
			}
			for(Action newAction : possibleActions) {
				try {
					SimplePlan p = new SimplePlan(plan.currentState.takeAction(newAction), actionList, goal);
	//
					if (newAction.command == Action.COMMAND.DROP) {
						if (!ConstraintCheck.isValidColumn(plan.currentState.world.get(newAction.column))) {
							return null;
						}
					} 
				} catch (Exception e) {
					Debug.print(e);
					continue;
					// The action was rejected, so we do nothing.
				}
			}
		}
//		return null; //No Plan found
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

	@Override
	public SimplePlan call() throws Exception {
		
		int MAX_PLANS = 1000;
				
		long start = System.currentTimeMillis();
		List<SimplePlan> plans = new ArrayList<SimplePlan>();
		int planSize = 0;
		while(planSize < MAX_PLANS) {
			SimplePlan plan = null;
			try {
				plan = solve(goal);
				if(plan != null) {
					planSize++;
					setMaxDepth(plan.actions.size());
					plans.add(plan);
				}
			} catch (Exception e) {
//				Debug.print(e);
				planSize++;
//				return getShortestPlan(plans);
			}
		}
		long elapsed = System.currentTimeMillis() - start;
		Debug.print(this + ": Plan solved in: " + elapsed + " ms.");
		return getShortestPlan(plans);
	}
	
	
	private static SimplePlan getShortestPlan(List<SimplePlan> plans) {
		if(plans == null || plans.isEmpty()) {
			return null;
		}
		SimplePlan shortestPlan = plans.remove(0);
		for(SimplePlan p : plans) {
			if(p.actions.size() < shortestPlan.actions.size()) {
				shortestPlan = p;
			}
		}
		return shortestPlan;
	}

}