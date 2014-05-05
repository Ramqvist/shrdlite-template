package src.planner;
import java.util.ArrayList;
import java.util.List;

import src.Debug;
import src.planner.data.SimplePlan;
import src.world.Entity;
import src.world.Goal;


public class BreathFirstSolver implements IGoalSolver {

	private List<List<Entity>> world;
	private Entity heldEntity;
	private List<Goal> goals;
	
	public BreathFirstSolver(List<List<Entity>> world, Entity heldEntity, List<Goal> goals) {
		this.world = world;
		this.heldEntity = heldEntity;
		this.goals = goals;
	}
	
	@Override
	public List<SimplePlan> solve() {
		BreathFirstPlanner planner = new BreathFirstPlanner(world, heldEntity);
		List<SimplePlan> plans = new ArrayList<SimplePlan>();
		int maxDepth = Integer.MAX_VALUE;

		for (Goal g : goals) {
			long start = System.currentTimeMillis();
			SimplePlan aPlan = planner.solve(g, maxDepth);
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
