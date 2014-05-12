package src.planner.data;

public class PlannerException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public PlannerException(String message) {
		super(message);
	}

	public PlannerException() {
		super();
	}
}
