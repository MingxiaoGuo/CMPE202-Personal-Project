
public class MethodParameter {

	private String parameterName;
	private String parameterType;
	
	public MethodParameter(String parameterName, String parameterType) {
		this.parameterName = parameterName;
		this.parameterType = parameterType;
	}

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

}
