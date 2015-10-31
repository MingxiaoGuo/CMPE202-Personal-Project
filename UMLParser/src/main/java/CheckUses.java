import java.util.ArrayList;
import java.util.HashSet;

public class CheckUses {

	private RealClass realClass;
	private ArrayList<RealClass> classList;
	
	public CheckUses(RealClass realClass, ArrayList<RealClass> classList) {
		this.setRealClass(realClass);
		this.setClassList(classList);
		checkUse();
	}
	
	private void checkUse() {
		if (realClass.getClassMethods() == null || realClass.getClassMethods().size() == 0) {
			return;
		}
		realClass.getClassMethods();
		HashSet<String> interfaceClass = new HashSet<String>();
		HashSet<String> allClass = new HashSet<String>();
		ArrayList<Integer> useIndex = null;
		ArrayList<ClassMethod> toBeDeleted = new ArrayList<ClassMethod>();
		for (RealClass eachClass : classList) {
			allClass.add(eachClass.getClassName());
			if (eachClass.isInterface()) {
				interfaceClass.add(eachClass.getClassName());
			}
		}
		for (ClassMethod classMethod : realClass.getClassMethods()) {
			if (classMethod.checkUseIndex(allClass) != null) {
				useIndex = classMethod.checkUseIndex(allClass);
			}
			if (useIndex != null && useIndex.size() != 0) {
				ClassRelation relation;
				ArrayList<Integer> deleteInterface = new ArrayList<Integer>();
				for (Integer integer : useIndex) {
					if (interfaceClass.contains(classMethod.getRealParameters().get(integer).getParameterType())) {
						deleteInterface.add(integer);
					}
				}
				for (Integer integer : deleteInterface) {
					useIndex.remove(integer);
				}
				for (int i = 0; i < useIndex.size(); i++) {
						relation = new ClassRelation(realClass.getClassName(),
								classMethod.getRealParameters().get(i).getParameterType(), RelatinType.DEPENDENCIES);
						realClass.getClassRelations().add(relation);
				}
				toBeDeleted.add(classMethod);
			}
		}
		// Remove methods
		if (useIndex != null && toBeDeleted.size() != 0) {
			for (ClassMethod classMethod : toBeDeleted) {
				realClass.getClassMethods().remove(classMethod);
			}
		}
	}

	public RealClass getRealClass() {
		return realClass;
	}

	public void setRealClass(RealClass realClass) {
		this.realClass = realClass;
	}

	public ArrayList<RealClass> getClassList() {
		return classList;
	}

	public void setClassList(ArrayList<RealClass> classList) {
		this.classList = classList;
	}
}
