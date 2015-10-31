

public class ClassType {

	private String className;
	private boolean isInterface;
	private Object[] extendFrom;
	private Object[] implementFrom;
	
	public ClassType(String className, boolean isInterface, Object[] extendFrom ,Object[] implementFrom) {
		this.className = className;
		this.isInterface = isInterface;
		this.extendFrom = extendFrom;
		this.implementFrom = implementFrom;
	}

	public String getClassName() {
		return className;
	}
	
	public boolean getIsInterface() {
		return isInterface;
	}
	
	public void display() {
		System.out.print(className + " is " + isInterface);
		if (implementFrom != null) {
			System.out.print(" implements ");
			for (int i = 0; i < implementFrom.length; i++) {
				System.out.print(implementFrom[i] + " and ");
			}
		}
		System.out.println();
	}
	
	public Object[] getImplementFrom() {
		return implementFrom;
	}
	
	public Object[] getExtendFrom() {
		return extendFrom;
	}
}
