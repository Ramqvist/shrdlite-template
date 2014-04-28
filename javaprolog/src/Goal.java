package src;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal is represented as a list of relations.
 * 
 */
public class Goal {

	private List<Relation> relations;

	public Goal(Relation relation) {
		this.relations = new ArrayList<>();
		this.relations.add(relation);
	}
	
	public Goal(List<Relation> relations) {
		this.relations = new ArrayList<>();
		for (Relation relation : relations) {
			this.relations.add(relation.copy());
		}
//		this.relations = relations;
	}

	public List<Relation> getRelations() {
		return relations;
	}

	@Override
	public String toString() {
		return "Goal: " + relations.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relations == null) ? 0 : relations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Goal)) {
			return false;
		}
		Goal other = (Goal) obj;
		if (relations == null) {
			if (other.relations != null) {
				return false;
			}
		} else if (!relations.equals(other.relations)) {
			return false;
		}
		return true;
	}

}
