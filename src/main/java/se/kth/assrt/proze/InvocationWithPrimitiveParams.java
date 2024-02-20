package se.kth.assrt.proze;

import java.util.List;

public class InvocationWithPrimitiveParams {
  String invocation;
  String methodInvoked;
  String declaringType;
  List<String> parameterTypes;

  public InvocationWithPrimitiveParams(String invocation,
                                       String methodInvoked,
                                       String declaringType,
                                       List<String> parameterTypes) {
    this.invocation = invocation;
    this.methodInvoked = methodInvoked;
    this.declaringType = declaringType;
    this.parameterTypes = parameterTypes;
  }
}
