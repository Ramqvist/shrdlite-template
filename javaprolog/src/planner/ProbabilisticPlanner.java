package src.planner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import src.Debug;
import src.constraints.ConstraintCheck;
import src.planner.data.Action;
import src.planner.data.PlannerException;
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
	
	public static Integer maxDepth = Integer.MAX_VALUE;
	private double maxSize = 1.0;
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
	
	private int getProbability(List<List<Entity>> world, Entity holding) {
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
	
	private SimplePlan solve(Goal goal) throws PlannerException, InterruptedException {
		List<Relation> relations = new ArrayList<Relation>();
		State startState = new State(world, relations, heldEntity);
		
		if (!ConstraintCheck.isValidWorld(world)) {
			Debug.print("World is not valid!");
			Debug.print(world);
			throw new PlannerException(this + ": World is not valid!");
		}
		
		int size = 0;
		SimplePlan currentPlan = new SimplePlan(startState, new ArrayList<Action>(), goal);
		while (true) {
			count++;
//			plan = queue.poll();
			if (hasReachedGoal(goal, currentPlan.currentState)) {
//				Debug.print(this + ": " + plan + " reached the goal state " + goal);
				Debug.print(this + ": Planning finished!");
//				Debug.print(this + ": Actions: " + plan.actions);
				Debug.print(this + ": Total iteration count: " + count);
				return currentPlan;
			}
			if (Thread.interrupted()) {
				try {
					throw new InterruptedException();
				} catch (InterruptedException e1) {
				}
				throw new InterruptedException();
			}

			/*
			 * Fill a list of possible actions to take. We only pick the actions
			 * that make sense to try. (Hopefully!)
			 */
			// Small optimization. No point in dropping in the same location
			// we last picked, or picking in the same location we last
			// dropped.


			List<Action> possibleActions = new ArrayList<Action>();
			for (int i = 0; i < world.size(); i++) {
				if (currentPlan.actions.size() == 0 || currentPlan.actions.get(currentPlan.actions.size() - 1).column != i) {
					if (currentPlan.currentState.isHolding()) {
						possibleActions.add(new Action(Action.COMMAND.DROP, i));
					} else {
						if (!currentPlan.currentState.world.get(i).isEmpty()) {
							possibleActions.add(new Action(Action.COMMAND.PICK, i));
						}
					}
				}
			}
			List<SimplePlan> possiblePlans = new ArrayList<>(possibleActions.size());
			List<Integer> scores = new ArrayList<>(possibleActions.size());
			int totalProbability = 0;

			maxSize += 0.001;
			for(Action newAction : possibleActions) {
				List<Action> actionList = new ArrayList<Action>(currentPlan.actions.size() + 1);
				for (Action c : currentPlan.actions) {
					actionList.add(c);
				}
				actionList.add(newAction);

				
				try {
					SimplePlan p = new SimplePlan(currentPlan.currentState.takeAction(newAction), actionList, goal);
					if (newAction.command == Action.COMMAND.DROP) {
						if (!ConstraintCheck.isValidColumn(p.currentState.world.get(newAction.column))) {
							continue;
						}
					}
					if(p.actions.size() > maxSize) {
						return null;
					}
					if (p.actions.size() > maxDepth) {
						return null;
					}
					possiblePlans.add(p);
					int probability = getProbability(world, p.currentState.holding);
					if(probability > 0) Debug.print(probability);
					probability = probability < 1 ? 1 : probability;
					scores.add(probability);
					totalProbability += probability;
				} catch (Exception e) {
					Debug.print(e);
					continue;
				}
			}
			int randNum = ran.nextInt(totalProbability);
			int sum = 0;
			int j = 0;
			if(scores.isEmpty()) {
				return null;
			}
			for(int i : scores) {
				sum += i;
				if(randNum < sum) {
					currentPlan = possiblePlans.get(j);
					break;
				}
				j++;
				if(j+1 == scores.size()) {
					currentPlan = possiblePlans.get(j);
					break;
				}
			}
//			Debug.print("totalProbability: " + totalProbability);
//			Debug.print("Sum: " + sum);
//			Debug.print("j: " + j);
		}
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
		
		int MAX_SAMPLES = 1000;
		boolean hasNonNullPlan = false;
				
		long start = System.currentTimeMillis();
		List<SimplePlan> plans = new ArrayList<SimplePlan>();
		int planSize = 0;
		while(planSize < MAX_SAMPLES || !hasNonNullPlan) {
			SimplePlan plan = null;
			try {
				plan = solve(goal);
				if(plan != null) {
					hasNonNullPlan = true;
					setMaxDepth(plan.actions.size());
					plans.add(plan);
				} else {
					planSize++;
//					Debug.print("No plan found max Size: " + maxSize);
				}
			} catch (InterruptedException e) {
//				Debug.print(e);
//				planSize++;
				return null;
//				return getShortestPlan(plans);
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
