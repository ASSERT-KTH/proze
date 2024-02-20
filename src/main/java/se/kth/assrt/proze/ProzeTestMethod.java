package se.kth.assrt.proze;

import java.util.List;

public class ProzeTestMethod {
  String declaringType;
  String name;
  String signature;
  List<InvocationWithPrimitiveParams> invocationWithPrimitiveParams;

  public ProzeTestMethod(String declaringType,
                         String name,
                         String signature,
                         List<InvocationWithPrimitiveParams> invocationsWithPrimitives) {
    this.declaringType = declaringType;
    this.name = name;
    this.signature = signature;
    this.invocationWithPrimitiveParams = invocationsWithPrimitives;
  }
}
