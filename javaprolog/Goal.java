import java.util.List;

/**
 * Goal is represented as a list of relations. 
 *
 */
public class Goal {
	
	private List<Relation> relations;

	public Goal(List<Relation> relations) {
		this.relations = relations;
	}
	
	public List<Relation> getRelations() {
		return relations;
	}
	
}
