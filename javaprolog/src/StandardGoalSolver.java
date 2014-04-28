package src;
import java.util.ArrayList;
import java.util.List;


public class StandardGoalSolver implements GoalSolver {

	private List<List<Entity>> world;
	private Entity heldEntity;
	private List<Goal> goals;
	
	public StandardGoalSolver(List<List<Entity>> world, Entity heldEntity, List<Goal> goals) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goals = goals;
	}
	
	@Override
	public List<Plan> solve() {
		Planner planner = new Planner(world, heldEntity);
		List<Plan> plans = new ArrayList<Plan>();
		int maxDepth = Integer.MAX_VALUE;

		for (Goal g : goals) {
			long start = System.currentTimeMillis();
			Plan aPlan = planner.solve(g, maxDepth);
			if (aPlan != null) {
				maxDepth = aPlan.actions.size();
				plans.add(aPlan);
			}
			long elapsed = System.currentTimeMillis() - start;
			Debug.print("Plan solved in: " + elapsed + " ms.");
		}
		
		return plans;
	}

}
