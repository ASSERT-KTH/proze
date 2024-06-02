package se.kth.assrt.proze.select;

import java.util.List;

public class ProzeTestMethod {
  private final String testClassName;
  private final String testName;
  private final String signature;
  private final int numAssertions;
  private final List<InvocationWithPrimitiveParams> invocationWithPrimitiveParams;

  public ProzeTestMethod(String testClassName,
                         String testName,
                         String signature,
                         List<InvocationWithPrimitiveParams> invocationsWithPrimitives,
                         int numAssertions) {
    this.testClassName = testClassName;
    this.testName = testName;
    this.signature = signature;
    this.invocationWithPrimitiveParams = invocationsWithPrimitives;
    this.numAssertions = numAssertions;
  }

  public String getTestClassName() {
    return testClassName;
  }
  public String getTestName() {
    return testName;
  }

  public String getSignature() {
    return signature;
  }

  public List<InvocationWithPrimitiveParams> getInvocationWithPrimitiveParams() {
    return invocationWithPrimitiveParams;
  }
}
