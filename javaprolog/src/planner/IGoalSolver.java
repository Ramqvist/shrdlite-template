package src.planner;
import java.util.List;

import src.planner.data.IPlan;
import src.planner.data.Plan;


public interface IGoalSolver {

	List<? extends IPlan> solve();
	
}
