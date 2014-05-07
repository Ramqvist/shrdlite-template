package src.planner.data;

import java.util.List;
import java.util.Stack;

import src.world.Entity;
import src.world.Relation;

// TODO: Implement.
public class StackState extends State {
	
	public Stack<List<List<Entity>>> worldStack;
	public Stack<Entity> holdingStack;

	public StackState(List<List<Entity>> world, List<Relation> relations, Entity heldEntity) {
		super(world, relations, heldEntity);
		worldStack = new Stack<>();
	}
	
	public StackState(StackState state) {
		super(state);
		worldStack = (Stack<List<List<Entity>>>) state.worldStack.clone();
	}
	
	@Override
	public StackState takeAction(Action a) throws NullPointerException, IllegalStateException {
		State newState = super.takeAction(a);
		world = newState.world;
		worldStack.push(newState.world);
		holding = newState.holding;
		holdingStack.push(newState.holding);
		return this;
	}
	
}
