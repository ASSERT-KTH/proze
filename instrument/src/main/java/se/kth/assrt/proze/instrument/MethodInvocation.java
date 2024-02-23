package se.kth.assrt.proze.instrument;

public class MethodInvocation {
  int invocationCount;
  Object parameters;
  boolean calledByInvokingTest;
  String stackTrace;

  public MethodInvocation() {
  }

  public void setInvocationCount(int invocationCount) {
    this.invocationCount = invocationCount;
  }

  public void setParameters(Object parameters) {
    this.parameters = parameters;
  }

  public void setCalledByInvokingTest(boolean calledByInvokingTest) {
    this.calledByInvokingTest = calledByInvokingTest;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }
}
