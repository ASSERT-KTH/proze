package se.kth.assrt.proze;

import java.util.List;

public class ProzeTargetMethod {
  String declaringType;
  String name;
  List<String> parameters;
  String returnType;
  String signature;

  public ProzeTargetMethod(String declaringType, String name, List<String> parameters, String returnType, String signature) {
    this.declaringType = declaringType;
    this.name = name;
    this.parameters = parameters;
    this.returnType = returnType;
    this.signature = signature;
  }
}
