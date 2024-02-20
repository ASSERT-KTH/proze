package se.kth.assrt.proze;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

public class ProzeTestMethodProcessor extends AbstractProcessor<CtMethod<?>> {

  private final List<ProzeTestMethod> testMethods = new LinkedList<>();

  private boolean methodIsNotEmpty(CtMethod<?> method) {
    Optional<CtBlock<?>> methodBody = Optional.ofNullable(method.getBody());
    return methodBody.isPresent() && !methodBody.get().getStatements().isEmpty();
  }

  private boolean methodHasTestAnnotation(CtMethod<?> method) {
    return (method.getAnnotations().stream()
            .anyMatch(a -> a.toString().contains("Test")));
  }

  private List<InvocationWithPrimitiveParams> getMethodInvocationsWithPrimitiveParameters(CtMethod<?> method) {
    List<InvocationWithPrimitiveParams> invocationsWithPrimitiveParams = new LinkedList<>();
    if (methodIsNotEmpty(method)) {
      List<CtStatement> statements = method.getBody().getStatements();
      for (CtStatement statement : statements) {
        List<CtInvocation<?>> invocationsInStatement = statement.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> invocation : invocationsInStatement) {
          if (!invocation.getArguments().isEmpty()
                  & !invocation.toString().toLowerCase().contains("assert")) {
            if (invocation.getArguments().stream().allMatch(e -> e.getType().isPrimitive()
                            || e.getType().getQualifiedName().equals("java.lang.String"))) {
              InvocationWithPrimitiveParams thisInvocation = new InvocationWithPrimitiveParams(invocation.prettyprint(),
                      invocation.getExecutable().getSignature(),
                      invocation.getExecutable().getDeclaringType().getQualifiedName(),
                      invocation.getArguments().stream()
                              .map(a -> a.getType().toString()).collect(Collectors.toList()));
              invocationsWithPrimitiveParams.add(thisInvocation);
            }
          }
        }
      }
    }
    return invocationsWithPrimitiveParams;
  }

  public List<ProzeTestMethod> getTestMethods() {
    return testMethods;
  }

  @Override
  public void process(CtMethod<?> method) {
    if (method.isPublic()
            & methodHasTestAnnotation(method)) {
      ProzeTestMethod testMethod = new ProzeTestMethod(
              method.getDeclaringType().getQualifiedName(),
              method.getSimpleName(),
              method.getSignature(),
              getMethodInvocationsWithPrimitiveParameters(method));
      testMethods.add(testMethod);
    }
  }
}
