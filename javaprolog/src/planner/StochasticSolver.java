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
import src.planner.data.Plan;
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
	
	public StochasticSolver(List<List<Entity>> world, Entity heldEntity, List<Goal> goals) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goals = goals;
	}
	
	public List<Plan> solve() {
		List<Plan> plans = new ArrayList<Plan>();
		
		Debug.print("Attempting to solve " + goals.size() + " goals.");
		ExecutorService executorService = Executors.newFixedThreadPool(goals.size());
		Set<Future<Plan>> futureSet = new HashSet<>();
		for (Goal goal : goals) {
			StochasticPlanner planner = new StochasticPlanner(world, heldEntity, goal);
			Future<Plan> future = executorService.submit(planner);
			futureSet.add(future);
			Debug.print("Submitted " + planner + " to be solve " + goal);
		}

		for (Future<Plan> future : futureSet) {
			try {
				Plan plan = future.get();
				if(plan != null) {
					plans.add(plan);
				}
				Debug.print(plan + " received!");
			} catch (InterruptedException | ExecutionException e) {
				Debug.print(e.getMessage());
			}
		}
		
		executorService.shutdownNow();
		
		Debug.print();
		Debug.print("All goals solved!");
		Debug.print("Received the following plans: " + plans);
		return plans;
	}
	
}
