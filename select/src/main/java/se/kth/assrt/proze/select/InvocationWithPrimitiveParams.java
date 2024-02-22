package se.kth.assrt.proze.select;

import java.util.List;

public class InvocationWithPrimitiveParams {
  String invocation;
  String fullMethodSignature;
  String methodDeclaringType;
  String methodName;
  List<String> methodParameterTypes;
  String methodReturnType;

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
}
