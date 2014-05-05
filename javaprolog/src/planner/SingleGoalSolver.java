package src.planner;
import java.util.ArrayList;
import java.util.List;

import src.Debug;
import src.planner.data.Plan;
import src.world.Entity;
import src.world.Goal;


public class SingleGoalSolver implements IGoalSolver {

	private List<List<Entity>> world;
	private Entity heldEntity;
	private List<Goal> goals;
	
	public SingleGoalSolver(List<List<Entity>> world, Entity heldEntity, List<Goal> goals) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goals = goals;
	}
	
	@Override
	public List<Plan> solve() {
		HeuristicPlanner planner = new HeuristicPlanner(world, heldEntity);
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
