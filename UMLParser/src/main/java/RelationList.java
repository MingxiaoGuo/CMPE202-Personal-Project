import java.util.ArrayList;

public class RelationList {

	private String className;
	private ArrayList<ClassRelation> relations;
	
	public RelationList(String className, ArrayList<ClassRelation> relations) {
		this.setClassName(className);
		this.setRelations(relations);
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public ArrayList<ClassRelation> getRelations() {
		return relations;
	}

	public void setRelations(ArrayList<ClassRelation> relations) {
		this.relations = relations;
	}

}
