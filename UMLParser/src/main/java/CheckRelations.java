import java.util.ArrayList;
/**
 * @author Marshall
 *
 */
public class CheckRelations {

	private ArrayList<RelationList> allRelations;
	
	public CheckRelations(ArrayList<RelationList> allRelations) {
		this.setAllRelations(allRelations);
	}

	public void checkAll() {
		for (RelationList relationList : allRelations) {
			checkRelationEach(relationList);
		}
	}
	
	private void checkRelationEach(RelationList singleList) {
		if (singleList == null || singleList.getRelations() == null || singleList.getRelations().size() == 0) {
			return;
		}
		String className = singleList.getClassName();
		
		String targetClass = "";
		ArrayList<ClassRelation> classRelations = singleList.getRelations();
		ArrayList<ClassRelation> targetToBeDeleted = new ArrayList<ClassRelation>();
		for (ClassRelation classRelation : classRelations) {
			targetClass = classRelation.getTargetClass();
			for (RelationList relationList : allRelations) {
				if (relationList == null || relationList.getRelations().size() == 0) {
					break;
				}
				String tempName = relationList.getClassName();
				if (tempName.contentEquals(targetClass)) { 
					for (ClassRelation targetRelation : relationList.getRelations()) {
						if (targetRelation.getTargetClass().contentEquals(className)) {
							targetToBeDeleted.add(targetRelation);
						}
					}
					for (ClassRelation willDeleteRelation : targetToBeDeleted) {
						relationList.getRelations().remove(willDeleteRelation);
					}
				}
			}
		}
	}

	public void freshClassRelation(ArrayList<RealClass> realClasses) {
		for (RealClass realClass : realClasses) {
			for (RelationList relationList : allRelations) {
				if (relationList == null || relationList.getRelations().size() == 0) {
					break;
				}
				if (relationList.getClassName() == realClass.getClassName()) {
					realClass.setRelationList(relationList);
				}
			}
		}
	}
	
	public void checkExtends(ArrayList<RealClass> realClasses) {
		for (RealClass realClass : realClasses) {
			String currentClassName = realClass.getClassName();
			for (RealClass otherClass : realClasses) {
				if (otherClass.getExtendsFrom() != null && otherClass.getExtendsFrom().size() != 0) {
					for (String string : otherClass.getExtendsFrom()) {
						if (string.contentEquals(currentClassName)) {
							if (realClass.getRelationList() == null) {
								realClass.setRelationList(new RelationList(realClass.getClassName(), new ArrayList<ClassRelation>()));
							}
							realClass.getRelationList().getRelations().add(new ClassRelation(currentClassName, otherClass.getClassName(), RelatinType.INHERITANCE));
						}
					}
				}
			}
		}
	}

	public void checkImplements(ArrayList<RealClass> realClasses) {
		for (RealClass realClass : realClasses) {
			String currentClassName = realClass.getClassName();
			for (RealClass otherClass : realClasses) {
				if (otherClass.getImplementsFrom() != null && otherClass.getImplementsFrom().size() != 0) {
					for (String string : otherClass.getImplementsFrom()) {
						if (string.contentEquals(currentClassName)) {
							
							if (realClass.getRelationList() == null) {
								realClass.setRelationList(new RelationList(realClass.getClassName(), new ArrayList<ClassRelation>()));
							}
							// TODO check for duplicate
							realClass.getRelationList().getRelations().add(new ClassRelation(currentClassName, otherClass.getClassName(), RelatinType.INTERFACE_INHERITANCE));
						}
					}
				}
			}
		}
	}
	
	public ArrayList<RelationList> getAllRelations() {
		return allRelations;
	}

	public void setAllRelations(ArrayList<RelationList> allRelations) {
		this.allRelations = allRelations;
	}

}
