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

  public void setParameters(Object[] parameters, String[] parameterTypes) {
    for (int i = 0; i < parameters.length; i++) {
      // handle commas by removing them :)
      // they are put back when generating tests
      if (parameterTypes[i].equals("java.lang.String") && parameters[i] != null) {
        parameters[i] = parameters[i].toString()
                .replaceAll(",", "PROZE-REDACTED-COMMA");
        if (parameters[i].toString().isEmpty()) {
          parameters[i] = "PROZE-EMPTY-STRING";
        }
      }
    }
    this.parameters = parameters;
  }

  public void setCalledByInvokingTest(boolean calledByInvokingTest) {
    this.calledByInvokingTest = calledByInvokingTest;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }
}
