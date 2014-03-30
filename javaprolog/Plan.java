import java.util.ArrayList;
import java.util.List;


public class Plan {

	public final List<Action> actions;
	
	public Plan(){
		actions = new ArrayList<Action>();
	}
	
	public void add(Action a){
		actions.add(a);
	}
	
	public boolean isEmpty() {
		return true; // TODO
	}
	
}
