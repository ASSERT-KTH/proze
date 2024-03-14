package se.kth.assrt.proze.select;

import java.util.List;

public class ProzeTestMethod {
  private final String testClassName;
  private final String testName;
  private final String signature;
  private final List<InvocationWithPrimitiveParams> invocationWithPrimitiveParams;

  public ProzeTestMethod(String testClassName,
                         String testName,
                         String signature,
                         List<InvocationWithPrimitiveParams> invocationsWithPrimitives) {
    this.testClassName = testClassName;
    this.testName = testName;
    this.signature = signature;
    this.invocationWithPrimitiveParams = invocationsWithPrimitives;
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
