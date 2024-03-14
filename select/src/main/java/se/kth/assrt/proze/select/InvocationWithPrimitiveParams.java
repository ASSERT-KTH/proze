package se.kth.assrt.proze.select;

import java.util.List;

public class InvocationWithPrimitiveParams {
  private final String invocation;
  private final String fullMethodSignature;
  private final String methodDeclaringType;
  private final String methodName;
  private final List<String> methodParameterTypes;
  private final String methodReturnType;

  public InvocationWithPrimitiveParams(String invocation,
                                       String fullMethodSignature,
                                       String methodDeclaringType,
                                       String methodName,
                                       List<String> methodParameterTypes,
                                       String methodReturnType) {
    this.invocation = invocation;
    this.fullMethodSignature = fullMethodSignature;
    this.methodDeclaringType = methodDeclaringType;
    this.methodName = methodName;
    this.methodParameterTypes = methodParameterTypes;
    this.methodReturnType = methodReturnType;
  }

  public String getInvocation() {
    return invocation;
  }

  public String getFullMethodSignature() {
    return fullMethodSignature;
  }

  public String getMethodDeclaringType() {
    return methodDeclaringType;
  }

  public String getMethodName() {
    return methodName;
  }

  public List<String> getMethodParameterTypes() {
    return methodParameterTypes;
  }

  public String getMethodReturnType() {
    return methodReturnType;
  }
}
