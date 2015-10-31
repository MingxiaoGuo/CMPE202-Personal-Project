
public class ClassRelation {

	private String currentClass;
	private String targetClass;
	private String relationship;
	
	public ClassRelation(String currentClass, String targetClass, String relationship) {
		this.setCurrentClass(currentClass);
		this.setTargetClass(targetClass);
		this.setRelationship(relationship);
	}

	public boolean valueEquals(ClassRelation otherRelation) {
		if (otherRelation == null) {
			return false;
		}
		if (this.currentClass.contentEquals(otherRelation.currentClass)) {
			if (this.targetClass.contentEquals(otherRelation.targetClass)) {
				if (this.relationship.contentEquals(otherRelation.relationship)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getCurrentClass() {
		return currentClass;
	}

	public void setCurrentClass(String currentClass) {
		this.currentClass = currentClass;
	}

}
