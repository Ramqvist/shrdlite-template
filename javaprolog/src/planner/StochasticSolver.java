package src.planner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import src.Debug;
import src.planner.data.SimplePlan;
import src.world.Entity;
import src.world.Goal;

/**
 * A planner that uses Randomized planner that uses Markov chain Monte Carlo
 * methods to determine the next step.
 */
public class StochasticSolver implements IGoalSolver {
	
	private List<List<Entity>> world;
	private Entity heldEntity;
	private List<Goal> goals;
	private ExecutorService executorService;
	
	public StochasticSolver(List<List<Entity>> world, Entity heldEntity, List<Goal> goals) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goals = goals;
	}
	
	public List<SimplePlan> solve() {
		List<SimplePlan> plans = new ArrayList<SimplePlan>();
		
		Debug.print("Attempting to solve " + goals.size() + " goals.");
		executorService = Executors.newFixedThreadPool(goals.size());
		Set<Future<SimplePlan>> futureSet = new HashSet<>();
		for (Goal goal : goals) {
			StochasticPlanner planner = new StochasticPlanner(world, heldEntity, goal);
			Future<SimplePlan> future = executorService.submit(planner);
			futureSet.add(future);
			Debug.print("Submitted " + planner + " to be solve " + goal);
		}

		for (Future<SimplePlan> future : futureSet) {
			try {
				SimplePlan plan = future.get();
				if (Thread.interrupted()) {
					return plans;
				}
				if(plan != null) {
					plans.add(plan);
				}
				Debug.print(plan + " received!");
			} catch (InterruptedException | ExecutionException e) {
				Debug.print(e.getMessage());
			}
		}
		
		executorService.shutdownNow();
		executorService = null;
		
		Debug.print();
		Debug.print("All goals solved!");
		Debug.print("Received the following plans: " + plans);
		return plans;
	}

	@Override
	public void reset() {
		StochasticPlanner.setMaxDepth(Integer.MAX_VALUE);
		if (executorService != null) {
			executorService.shutdownNow();
		}
	}
	
}
