import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 * 
 * @author Marshall
 *
 */
public class RealClass {

	private String className;
	private String realFilePath;
	private boolean isInterface;
	private HashSet<String> classList;
	private ArrayList<ClassRelation> classRelations;
	private ArrayList<ClassAttribute> classAttributes;
	private ArrayList<ClassMethod> classMethods;
	private ArrayList<String> extendsFrom;
	private ArrayList<String> implementsFrom;
	private CompilationUnit cUnit;
	private RelationList relationList;
	private String UMLClassName;
	private String UMLClassRelation;
	private String UMLClassDetail;

	public RealClass(String filePath, HashSet<String> classList) {

		this.realFilePath = filePath;
		this.classList = classList;
		this.className = getClassNameFromPath(realFilePath);
		// Initialize all lists
		setClassRelations(new ArrayList<ClassRelation>());
		setClassAttributes(new ArrayList<ClassAttribute>());
		setClassMethods(new ArrayList<ClassMethod>());
		setExtendsFrom(new ArrayList<String>());
		setImplementsFrom(new ArrayList<String>());
		setRelationList(new RelationList(className, classRelations));
		// Initialize all UML queries
		this.setUMLClassDetail("");
		this.UMLClassName = "";
		this.setUMLClassRelation("");
		// Get Compilation Unit
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(realFilePath);
			cUnit = JavaParser.parse(inputStream);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Get data
		if (cUnit != null) {
			// Find extends & implements
			getExtends();
			// Find Attributes
			getAttributes();
			// Find Methods
			getMethods();
			// Find Constructor
			getConstructor();
			// Alter Attributes
			// invoked in TestRun.java(main function), due to all class information needed
			// Alter Methods
			// Same as Alter Attributes
		} else {
			System.out.println("CompilationUnit is null");
		}
		if (classRelations != null && classRelations.size() != 0 && relationList != null) {
			this.setRelationList(new RelationList(this.getClassName(), this.classRelations));
		} else {
			refreshRelationList();
		}
	}

	/**
	 * Check relationList and classRelations both have the same relationships
	 * Eventually, relationList must keep synchronous to classRelations
	 * Using two nested loop
	 */
	public void refreshRelationList() {
		if (classRelations == null || classRelations.size() == 0) {
			return;
		}
		if (relationList == null || relationList.getRelations() == null) {
			this.setRelationList(new RelationList(className, classRelations));
		} else {
			for (ClassRelation classRelation : classRelations) {
				for (ClassRelation iterateRelation : relationList.getRelations()) {
					if (!classRelation.valueEquals(iterateRelation)) {
						relationList.getRelations().add(classRelation);
					}
				}
			}
		}
	}

	/**
	 * Used for detecting inheritance relationship
	 * Used for detecting whether this class is interface or not
	 * @author Marshall
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class ClassInterfaceVisitor extends VoidVisitorAdapter {
		@Override
		public void visit(ClassOrInterfaceDeclaration n, Object arg) {
			isInterface = n.isInterface();
			Object[] implement = null;
			Object[] extend = null;
			if (n.getExtends() != null) {
				extend = n.getExtends().toArray();
				for (Object object : extend) {
					extendsFrom.add(object.toString());
				}
			}
			if (n.getImplements() != null) {
				implement = n.getImplements().toArray();
				for (Object object : implement) {
					implementsFrom.add(object.toString());
				}
			}
		}
	}

	/**
	 * Get the java file and convert it into a class name.
	 * Operating System has to be considered.
	 * @param path java file path
	 * @return The name of this class
	 */
	private String getClassNameFromPath(String path) {
		Properties properties = System.getProperties();
		String osName = properties.getProperty("os.name").toLowerCase();
		if (osName.contains("windows")) {
			int beginIndex = path.lastIndexOf("\\");
			int endIndex = path.lastIndexOf(".java");
			return path.substring(beginIndex + 1, endIndex);
		} else {
			int beginIndex = path.lastIndexOf("/");
			int endIndex = path.lastIndexOf(".java");
			return path.substring(beginIndex + 1, endIndex);
		}

	}

	@SuppressWarnings("unchecked")
	private void getExtends() {
		new ClassInterfaceVisitor().visit(cUnit, null);
	}

	/**
	 * Get every attributes this class has.
	 * But no protected attributes.
	 */
	private void getAttributes() {
		if (cUnit == null) {
			return;
		}
		// Initialize parameters that building a new attribute instance needs
		ClassAttribute attribute = null;
		String attributeName = "";
		String accessModifier = "";
		String attributeType = "";
		for (TypeDeclaration typeDeclaration : cUnit.getTypes()) {
			List<BodyDeclaration> memebers = typeDeclaration.getMembers();
			if (memebers == null) {
				return;
			}
			for (BodyDeclaration bodyDeclaration : memebers) {
				FieldDeclaration fieldDeclaration;
				try {
					// Conversion here may cause exception
					fieldDeclaration = (FieldDeclaration) bodyDeclaration;
					accessModifier = GetModifier(fieldDeclaration.getModifiers());
					if (accessModifier == "+" || accessModifier == "-") {
						attributeName = fieldDeclaration.getVariables().get(0).getId().getName();
						attributeType = fieldDeclaration.getType().toString();
						attribute = new ClassAttribute(attributeName, accessModifier, attributeType);
						this.classAttributes.add(attribute);
					}
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * Get only public class method in this class
	 */
	private void getMethods() {
		if (cUnit == null) {
			return;
		}
		String methodReturnType;
		String methodName;
		String methodModifier;
		MethodDeclaration methodDeclaration;
		ArrayList<Parameter> parametersList = null;
		for (TypeDeclaration typeDeclaration : cUnit.getTypes()) {
			List<BodyDeclaration> memebers = typeDeclaration.getMembers();
			if (memebers == null) {
				return;
			}
			for (BodyDeclaration bodyDeclaration : memebers) {
				try {
					methodDeclaration = (MethodDeclaration) bodyDeclaration;
					methodModifier = GetModifier(methodDeclaration.getModifiers());
					if (methodModifier == "+") {
						methodReturnType = methodDeclaration.getType().toString();
						methodName = methodDeclaration.getName().toString();
						try {
							// Fetch each method's parameters
							if (methodDeclaration.getParameters() != null
									&& methodDeclaration.getParameters().size() != 0) {
								parametersList = new ArrayList<Parameter>();
								for (Parameter parameter : methodDeclaration.getParameters()) {
									parametersList.add(parameter);
								}
							}
						} catch (Exception e) {

						}
						classMethods.add(new ClassMethod(parametersList, methodReturnType, methodName, methodModifier));
						parametersList = null;
					}
				} catch (Exception e) {
				}
			}
		}

	}

	/**
	 * Get constructors in this class
	 */
	private void getConstructor() {
		if (cUnit == null) {
			return;
		}
		ConstructorDeclaration constructorDeclaration;
		String constructorModifier = "";
		ArrayList<Parameter> constructorParameters = null;
		String constructorName = "";
		for (TypeDeclaration typeDeclaration : cUnit.getTypes()) {
			List<BodyDeclaration> memebers = typeDeclaration.getMembers();
			if (memebers == null) {
				return;
			}

			for (BodyDeclaration bodyDeclaration : memebers) {
				try {
					constructorDeclaration = (ConstructorDeclaration) bodyDeclaration;
					constructorModifier = GetModifier(constructorDeclaration.getModifiers());
					if (constructorModifier == "+") {
						constructorParameters = new ArrayList<Parameter>();
						if (constructorDeclaration.getParameters() != null
								&& constructorDeclaration.getParameters().size() != 0) {
							for (Parameter parameter : constructorDeclaration.getParameters()) {
								constructorParameters.add(parameter);
							}
						}
						constructorName = constructorDeclaration.getName();
						classMethods.add(new ClassMethod(constructorParameters, "void", constructorName, "+"));
					}
				} catch (Exception e) {
					// handle exception
				}
			}
		}
	}

	/**
	 * Looking for dependencies in method's body
	 * @param allClasses 
	 */
	public void findDependBody(ArrayList<RealClass> allClasses) {
		// Every possible class has to be considered
		// Therefore all classes passed in
		HashSet<String> allClassName = new HashSet<String>();
		for (RealClass realClass : allClasses) {
			allClassName.add(realClass.getClassName());
		}
		for (TypeDeclaration typeDeclaration : cUnit.getTypes()) {
			List<BodyDeclaration> bodyDeclarations = typeDeclaration.getMembers();
			try {
				MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclarations.get(0);
				if (methodDeclaration.getName().contentEquals("main")) {
					// Block here means all lines inside {}
					BlockStmt blockStmt = methodDeclaration.getBody();
					List<Statement> statements = blockStmt.getStmts();
					for (Statement statement : statements) {
						String stmtLine = statement.toString();
						// Only care about dependencies, therefore no need for scanning whole line
						// Just looking at strings before "="
						if (stmtLine.contains("=")) {
							stmtLine = stmtLine.substring(0, stmtLine.indexOf('='));
							String variable = stmtLine.substring(0, stmtLine.indexOf(' '));
							if (allClassName.contains(variable)) {
								ClassRelation relation = new ClassRelation(this.getClassName(), variable,
										RelatinType.DEPENDENCIES);
								classRelations.add(relation);
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	}

	/**
	 * Looking for dependencies in attributes
	 * @param allClasses
	 */
	public void alterAttributeList(ArrayList<RealClass> allClasses) {
		if (this.classAttributes == null || this.classAttributes.size() == 0) {
			return;
		}
		ClassRelation relation = null;
		ClassAttribute classAttribute = null;
		ArrayList<Integer> removeMethodIndex = new ArrayList<Integer>();
		ArrayList<Integer> removeAttributeIndex = new ArrayList<Integer>();
		for (int i = 0; i < classAttributes.size(); i++) {
			classAttribute = classAttributes.get(i);
			String type = classAttribute.getAttributeType();
			// Do nothing when type is primitive type or String type
			// Add relation into classRelations and delete this attribute when type contains Collection<>
			Pattern pattern = Pattern.compile("Collection<.*>", Pattern.CASE_INSENSITIVE);
			Matcher m = pattern.matcher(type);
			if (m.matches()) {
				int beginIndex = type.indexOf('<');	// Get the type out of string
				int endIndex = type.indexOf('>');
				String tempType = type.substring(beginIndex + 1, endIndex);
				// under the requirement, no need for checking if type is in the class list 
				relation = new ClassRelation(this.getClassName(), tempType, RelatinType.OneToMany);
				this.classRelations.add(relation);
				// Delete the attribute
				removeAttributeIndex.add(i);
				relation = null;
			}
			// Arrays: [] --> (*)
			else if (type.contains("[]")) {
				int openIndex = type.indexOf('[');
				type = type.substring(0, openIndex);
				type += "(*)";
				classAttribute.setAttributeType(type);
			}
			// Other situation(simple declaration)
			else if (classList.contains(type)) {
				relation = new ClassRelation(this.getClassName(), type, RelatinType.OneToOne);
				this.classRelations.add(relation);
				// Attributes can not be deleted during the loop
				removeAttributeIndex.add(i);
				relation = null;
			}
			// If Getter and Setter of a attribute are found
			// This attribute should become public
			// Getter and Setter should be deleted
			else if (classAttribute.getAccessModifier() == "-") {
				String getter = "get" + classAttribute.getAttributeName();
				String setter = "set" + classAttribute.getAttributeName();
				// Find the indexes of getter and setter in method list
				int getterIndex = -1;
				int setterIndex = -1;
				for (int index = 0; index < this.classMethods.size(); index++) {
					if (this.classMethods.get(index).getMethodName().toLowerCase().contentEquals(getter)) {
						getterIndex = index;
					} else if (this.classMethods.get(index).getMethodName().toLowerCase().contentEquals(setter)) {
						setterIndex = index;
					}
				}
				// Can not delete method within this loop
				// Mark the indexes, delete them after loop is end 
				if (getterIndex != -1 && setterIndex != -1) {
					removeMethodIndex.add(getterIndex);
					removeMethodIndex.add(setterIndex);
					classAttribute.setAccessModifier("+");
				}
			}
			// If it's protected, remove it
			else if (classAttribute.getAccessModifier() == "#") {
				removeAttributeIndex.add(i);
			}
		}
		// Delete those attributes
		for (int j = removeAttributeIndex.size() - 1; j >= 0; j--) {
			classAttributes.remove((int) removeAttributeIndex.get(j));
		}
		// Delete the getter and setter
		for (int j = removeMethodIndex.size() - 1; j >= 0; j--) {
			classMethods.remove((int) removeMethodIndex.get(j));
		}
	}

	/**
	 * Looking for dependencies inside every method;
	 * Parameters may contain dependencies relation; 
	 * Only care about dependencies to interface classes.
	 * @param allClasses Used for retrieving interface classes
	 */
	public void alterMethodList(ArrayList<RealClass> allClasses) {
		if (classMethods == null || classMethods.size() == 0) {
			return;
		}
		// Get interface classes
		HashSet<String> interfaceClass = new HashSet<String>();
		for (RealClass realClass : allClasses) {
			if (realClass.isInterface) {
				interfaceClass.add(realClass.getClassName());
			}
		}
		ArrayList<Integer> useIndex = null;
		for (ClassMethod classMethod : classMethods) {
			if (classMethod.checkUseIndex(classList) != null) {
				useIndex = classMethod.checkUseIndex(classList);
			}
			if (classMethod.getMethodName().contains("attach") || classMethod.getMethodName().contains("detach")) {
				break;
			}
			if (useIndex != null) {
				ClassRelation relation;
				for (Integer index : useIndex) {
					if (classMethod.getRealParameters() != null && classMethod.getRealParameters().size() != 0) {
						String parameterType = classMethod.getRealParameters().get(index).getParameterType();
						if (interfaceClass.contains(parameterType)) {
							relation = new ClassRelation(this.getClassName(), parameterType, RelatinType.DEPENDENCIES);
							classRelations.add(relation);
						}
					}
				}
			}
			useIndex = null;
		}
	}

	public void convertUMLQuery(ArrayList<RealClass> realClasses) {
		convertUMLClassName();
		convertUMLClassDetail();
		convertUMLRelation(realClasses);
	}

	public void convertUMLClassName() {
		UMLClassName = "[";

		if (isInterface) {
			// Normal interface notation <<>> will not
			// So an utf-8 coded «» does the work
			UMLClassName += "%C2%ABInterface%C2%BB;";
		}
		UMLClassName += this.className + "]";
	}

	public void convertUMLClassDetail() {
		setUMLClassDetail("[");
		if (UMLClassName == "") {
			return;
		}
		if (isInterface) {
			setUMLClassDetail(getUMLClassDetail() + "%C2%ABInterface%C2%BB;");
		}
		setUMLClassDetail(getUMLClassDetail() + this.className);
		// add attributes
		if (this.classAttributes != null && this.classAttributes.size() != 0) {
			setUMLClassDetail(getUMLClassDetail() + "|");
			for (ClassAttribute classAttribute : classAttributes) {
				String attributeStr = "";
				attributeStr = classAttribute.getAccessModifier() + classAttribute.getAttributeName() + ":"
						+ classAttribute.getAttributeType() + ";";
				setUMLClassDetail(getUMLClassDetail() + attributeStr);
			}
		}
		// add methods
		if (this.classMethods != null && this.classMethods.size() != 0) {
			setUMLClassDetail(getUMLClassDetail() + "|");
			for (ClassMethod method : classMethods) {
				String methodStr = "";
				String methodAttriStr = "";
				if (method.getRealParameters() != null && method.getRealParameters().size() != 0) {
					for (MethodParameter methodParameter : method.getRealParameters()) {
						String methodPara = methodParameter.getParameterType();
						// If parameter's type is array, it will be changed into (*)
						if (methodPara.toString().contains("[]")) {
							methodAttriStr += methodParameter.getParameterName() + ":"
									+ methodPara.substring(0, methodPara.indexOf('[')) + "(*)";
						} else {
							methodAttriStr += methodParameter.getParameterName() + ":"
									+ methodParameter.getParameterType();
						}
					}
					methodStr = method.getMethodModifier() + method.getMethodName() + "(" + methodAttriStr + ")" + ":"
							+ method.getMethodReturnType() + ";";
					setUMLClassDetail(getUMLClassDetail() + methodStr);
				} else {
					methodStr = method.getMethodModifier() + method.getMethodName() + "(" + methodAttriStr + ")" + ":"
							+ method.getMethodReturnType() + ";";
					setUMLClassDetail(getUMLClassDetail() + methodStr);
				}
			}
		}
		setUMLClassDetail(getUMLClassDetail() + "],");
	}

	/**
	 * Convert relations with all classes into UML query
	 * @param classList
	 */
	public void convertUMLRelation(ArrayList<RealClass> classList) {
		if (relationList == null || relationList.getRelations() == null || relationList.getRelations().size() == 0) {
			return;
		}

		String umlTargetClassName = "";
		if (getRelationList() == null || getRelationList().getRelations() == null
				|| getRelationList().getRelations().size() == 0 || UMLClassName == "") {
			return;
		}
		for (ClassRelation classRelation : getRelationList().getRelations()) {
			umlTargetClassName = "[" + classRelation.getTargetClass() + "]";
			for (RealClass realClass : classList) {
				if (realClass.getClassName().contains(classRelation.getTargetClass())) {
					if (realClass.isInterface) {
						umlTargetClassName = "[%C2%ABInterface%C2%BB;" + realClass.getClassName() + "]";
					}
				}
			}

			setUMLClassRelation(
					getUMLClassRelation() + UMLClassName + classRelation.getRelationship() + umlTargetClassName + ",");
		}
	}

	/**
	 * Convert java code modifier into UML symbol
	 * @param modifier java code modifier
	 * @return + for public; - for private; # for protected
	 */
	private String GetModifier(int modifier) {
		if (ModifierSet.isPrivate(modifier)) {
			return "-";
		}
		if (ModifierSet.isPublic(modifier)) {
			return "+";
		}
		if (ModifierSet.isProtected(modifier)) {
			return "#";
		} else {
			return "";
		}
	}

	public void displayClass() {
		System.out.println("############################################");
		System.out.println("(" + this.getClassName() + ")");
		if (this.getExtendsFrom() != null && this.getExtendsFrom().size() != 0) {
			System.out.println("__________________");
			System.out.print("extends from ");
			for (String string : this.getExtendsFrom()) {
				System.out.println(string);
			}
		}
		if (this.getClassAttributes() != null && this.getClassAttributes().size() != 0) {
			System.out.println("________Attributes________");
			for (ClassAttribute attribute : this.getClassAttributes()) {
				System.out.println(attribute.getAccessModifier() + attribute.getAttributeName() + ":"
						+ attribute.getAttributeType());
			}
		}
		if (this.getClassMethods() != null && this.getClassMethods().size() != 0) {
			System.out.println("_________Methods_________");
			String parameterStr = "";
			for (ClassMethod classMethod : this.getClassMethods()) {
				ArrayList<MethodParameter> parameters = classMethod.getRealParameters();
				if (parameters != null && parameters.size() != 0) {
					for (MethodParameter parameter : parameters) {
						parameterStr += parameter.getParameterName() + ":" + parameter.getParameterType() + ", ";
					}
					parameterStr = parameterStr.substring(0, parameterStr.length() - 1 - 1);
				}

				System.out.println(classMethod.getMethodModifier() + classMethod.getMethodName() + " : "
						+ classMethod.getMethodReturnType() + " (" + parameterStr + ")");
			}
		}
		if (this.getClassRelations() != null && this.getClassRelations().size() != 0) {
			System.out.println("_________Relations__________" + this.getRelationList().getRelations().size());
			for (ClassRelation relation : this.getRelationList().getRelations()) {
				System.out.println(className + relation.getRelationship() + relation.getTargetClass());
			}
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public ArrayList<ClassAttribute> getClassAttributes() {
		return classAttributes;
	}

	public void setClassAttributes(ArrayList<ClassAttribute> classAttributes) {
		this.classAttributes = classAttributes;
	}

	public ArrayList<ClassMethod> getClassMethods() {
		return classMethods;
	}

	public void setClassMethods(ArrayList<ClassMethod> classMethods) {
		this.classMethods = classMethods;
	}

	public ArrayList<String> getExtendsFrom() {
		return extendsFrom;
	}

	public void setExtendsFrom(ArrayList<String> extendsFrom) {
		this.extendsFrom = extendsFrom;
	}

	public ArrayList<String> getImplementsFrom() {
		return implementsFrom;
	}

	public void setImplementsFrom(ArrayList<String> implementsFrom) {
		this.implementsFrom = implementsFrom;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public ArrayList<ClassRelation> getClassRelations() {
		return classRelations;
	}

	public void setClassRelations(ArrayList<ClassRelation> classRelations) {
		this.classRelations = classRelations;
	}

	public RelationList getRelationList() {
		return relationList;
	}

	public void setRelationList(RelationList relationList) {
		this.relationList = relationList;
	}

	public String getUMLClassDetail() {
		return UMLClassDetail;
	}

	public void setUMLClassDetail(String uMLClassDetail) {
		UMLClassDetail = uMLClassDetail;
	}

	public String getUMLClassRelation() {
		return UMLClassRelation;
	}

	public void setUMLClassRelation(String uMLClassRelation) {
		UMLClassRelation = uMLClassRelation;
	}

}
