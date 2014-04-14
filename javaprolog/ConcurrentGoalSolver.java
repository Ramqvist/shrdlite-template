import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ConcurrentGoalSolver implements GoalSolver {
	
	private List<List<Entity>> world;
	private Entity heldEntity;
	private List<Goal> goals;
	
	public ConcurrentGoalSolver(List<List<Entity>> world, Entity heldEntity, List<Goal> goals) {
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
			ConcurrentPlanner planner = new ConcurrentPlanner(world, heldEntity, goal);
			Future<Plan> future = executorService.submit(planner);
			futureSet.add(future);
			Debug.print("Submitted " + planner + " to be solve " + goal);
		}

		for (Future<Plan> future : futureSet) {
			try {
				Plan plan = future.get();
				plans.add(plan);
				Debug.print(plan + " received!");;
			} catch (InterruptedException e) {
				Debug.print(e.getMessage());
			} catch (ExecutionException e) {
				Debug.print(e.getMessage());
				e.printStackTrace();
			}
		}
		
		executorService.shutdownNow();
		
		Debug.print("All goals solved!");
		Debug.print(plans);
		return plans;
	}
	
}
