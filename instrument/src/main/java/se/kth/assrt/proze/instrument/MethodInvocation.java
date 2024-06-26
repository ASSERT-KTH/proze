package se.kth.assrt.proze.instrument;

public class MethodInvocation {
  Object parameters;
  boolean calledByInvokingTest;
  int methodTestDistance;
  String invokingTest;
  String stackTrace;

  public MethodInvocation() {
  }

  public void setParameters(Object[] parameters, String[] parameterTypes) {
    for (int i = 0; i < parameters.length; i++) {
      // handle commas by removing them :)
      // they are put back when generating tests
      if (parameterTypes[i].equals("java.lang.String")) {
        if (parameters[i] != null) {
          parameters[i] = parameters[i].toString()
                  .replaceAll(",", "PROZE-REDACTED-COMMA");
          if (parameters[i].toString().isEmpty()) {
            parameters[i] = "PROZE-EMPTY-STRING";
          }
        } else {
          parameters[i] = "PROZE-NULL-STRING";
        }
      }
    }
    this.parameters = parameters;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public void setCalledByInvokingTest(boolean calledByInvokingTest) {
    this.calledByInvokingTest = calledByInvokingTest;
  }

  public void setInvokingTest(String invokingTest) {
    this.invokingTest = invokingTest;
  }

  public void setMethodTestDistance(int distance) {
    this.methodTestDistance = distance;
  }

  public boolean isCalledByInvokingTest() {
    return calledByInvokingTest;
  }
}
