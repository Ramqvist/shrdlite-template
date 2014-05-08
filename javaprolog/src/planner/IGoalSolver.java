package src.planner;
import java.util.List;

import src.planner.data.IPlan;


public interface IGoalSolver {

	List<? extends IPlan> solve();
	
	public enum PlannerAlgorithm {
		BREADTH_FIRST,
		HEURISTIC,
		PROBABILITY,
		LIMITED_HEURISTIC,
		STOCHASTIC
	}
	
}
