import java.util.ArrayList;
import java.util.List;


/**
 * 	Representation of world state. Could be useful to have both array 
 * 	and relation representations in the same object.
 */

public class State {
	
	List<List<Entity>> world;
	List<Relation> relations;
	Entity holding = null;
	
	public State(List<List<Entity>> world, List<Relation> relations) {
		this.world = world;
		this.relations = relations;
	}
	
	public State(State state) {
		this.relations = new ArrayList<Relation>();
		for(Relation r : state.relations) {
			this.relations.add(r.copy());
		}
		this.world = new ArrayList<List<Entity>>();
		for(List<Entity> entityList : state.world) {
			List<Entity> l = new ArrayList<Entity>();
			for(Entity ent : entityList) {
				l.add(ent);
			}
			world.add(l);
		}
		if(state.holding != null) {
			this.holding = state.holding;
		}
	}

	public boolean exist(Relation r) {
		return relations.contains(r);
	}
	
	public State takeAction(Action a) throws NullPointerException, IllegalStateException {
		State s = new State(this);
		if(a.command == Action.COMMAND.DROP) {
			if(s.holding == null) {
				throw new NullPointerException("Not holding anything.");
			}
			s.world.get(a.column).add(s.holding);
			s.holding = null;
		} else {
			if(s.holding != null) {
				throw new IllegalStateException("Already holding a object.");
			}
			if(s.world.get(a.column).isEmpty()) {
				throw new IllegalStateException("No object to pick up.");
			}
			s.holding = s.world.get(a.column).remove(s.world.get(a.column).size() - 1);
		}
		return s;
	}

	public boolean isHolding() {
		return holding != null;
	}
	
	@Override
	public String toString() {
		return "(State " + world + " : " + relations + ")";
	}
	
}
