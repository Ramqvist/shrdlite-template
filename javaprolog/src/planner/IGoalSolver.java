package src.planner;
import java.util.List;

import src.planner.data.IPlan;


public interface IGoalSolver {

	List<? extends IPlan> solve();
	
	public enum PlannerAlgorithm {
		BREADTH_FIRST_PLANNER,
		CONCURRENT_PLANNER,
		HEURISTIC_PLANNER,
		LIMITED_HEURISTIC_PLANNER,
		PROBABILITY_PLANNER
	}
	
}
