
public class ClassAttribute {

	private String attributeName;
	private String accessModifier;
	private String attributeType;
	
	public ClassAttribute(String attributeName, String accessModifier, String attributeType) {
		this.setAttributeName(attributeName);
		this.setAccessModifier(accessModifier);
		this.setAttributeType(attributeType);
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAccessModifier() {
		return accessModifier;
	}

	public void setAccessModifier(String accessModifier) {
		this.accessModifier = accessModifier;
	}

	public String getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

}
