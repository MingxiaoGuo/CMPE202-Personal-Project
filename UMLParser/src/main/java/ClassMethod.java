import java.util.ArrayList;
import java.util.HashSet;

import japa.parser.ast.body.Parameter;

public class ClassMethod {
	
	private ArrayList<Parameter> parameters;
	private ArrayList<MethodParameter> realParameters;
	private String methodReturnType;
	private String methodName;
	private String methodModifier;
	
	public ClassMethod(ArrayList<Parameter> parameters, String returnType, String methodName, String methodModifier) {
		this.setParameters(parameters);
		this.setMethodReturnType(returnType);
		this.setMethodName(methodName);
		this.setMethodModifier(methodModifier);
		convertParameterList();
	}
	
	private void convertParameterList() {
		if (parameters == null || parameters.size() == 0) {
			return;
		}
		realParameters = new ArrayList<MethodParameter>();
		for (Parameter parameter : parameters) {
			realParameters.add(new MethodParameter(parameter.getId().toString(), parameter.getType().toString()));
		}
	}
	/**
	 * Indexes of parameters that dependent on other classes
	 * @param classList
	 * @return List of indexes of parameters in original parameterList
	 */
	public ArrayList<Integer> checkUseIndex(HashSet<String> classList) {
		if (realParameters == null || realParameters.size() == 0) {
			return null;
		}
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < realParameters.size(); i++) {
			if (classList.contains(realParameters.get(i).getParameterType())) {
				result.add(i);
			}
		}
		if (result.size() != 0) {
			return result;
		}
		return null;
	}
	
	
	public ArrayList<MethodParameter> getRealParameters() {
		return realParameters;
	}
	
	public void setRealParameters(ArrayList<MethodParameter> realParameters) {
		this.realParameters = realParameters;
	}


	public void setParameters(ArrayList<Parameter> parameters) {
		this.parameters = parameters;
	}

	public String getMethodReturnType() {
		return methodReturnType;
	}

	public void setMethodReturnType(String methodReturnType) {
		this.methodReturnType = methodReturnType;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodModifier() {
		return methodModifier;
	}

	public void setMethodModifier(String methodModifier) {
		this.methodModifier = methodModifier;
	}

}
