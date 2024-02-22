package se.kth.assrt.proze.select;

import java.util.List;

public class ProzeTestMethod {
  String testClassName;
  String testName;
  String signature;
  List<InvocationWithPrimitiveParams> invocationWithPrimitiveParams;

  public ProzeTestMethod(String testClassName,
                         String testName,
                         String signature,
                         List<InvocationWithPrimitiveParams> invocationsWithPrimitives) {
    this.testClassName = testClassName;
    this.testName = testName;
    this.signature = signature;
    this.invocationWithPrimitiveParams = invocationsWithPrimitives;
  }
}
