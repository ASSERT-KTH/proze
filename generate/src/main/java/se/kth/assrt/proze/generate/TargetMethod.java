package se.kth.assrt.proze.generate;

import java.util.List;
import java.util.Set;

public class TargetMethod {
  String fullMethodSignature;
  String declaringType;
  String methodName;
  List<String> parameters;
  List<String> invokedByTests;
  List<String> unionProdAndTestArgs;
  Set<String> testsThatInvokeDirectly;

  public String getFullMethodSignature() {
    return fullMethodSignature;
  }

  public void setFullMethodSignature(String fullMethodSignature) {
    this.fullMethodSignature = fullMethodSignature;
  }

  public String getDeclaringType() {
    return declaringType;
  }

  public void setDeclaringType(String declaringType) {
    this.declaringType = declaringType;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  public List<String> getInvokedByTests() {
    return invokedByTests;
  }

  public void setInvokedByTests(List<String> invokedByTests) {
    this.invokedByTests = invokedByTests;
  }

  public List<String> getUnionProdAndTestArgs() {
    return unionProdAndTestArgs;
  }

  public void setUnionProdAndTestArgs(List<String> unionProdAndTestArgs) {
    this.unionProdAndTestArgs = unionProdAndTestArgs;
  }

  public Set<String> getTestsThatInvokeDirectly() {
    return testsThatInvokeDirectly;
  }

  public void setTestsThatInvokeDirectly(Set<String> testsThatInvokeDirectly) {
    this.testsThatInvokeDirectly = testsThatInvokeDirectly;
  }
}
