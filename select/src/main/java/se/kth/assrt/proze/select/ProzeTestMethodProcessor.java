package se.kth.assrt.proze.select;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class ProzeTestMethodProcessor extends AbstractProcessor<CtMethod<?>> {

  private final List<ProzeTestMethod> testMethods = new LinkedList<>();

  private final Set<String> setOfTestClasses = new LinkedHashSet<>();

  private boolean methodIsNotEmpty(CtMethod<?> method) {
    Optional<CtBlock<?>> methodBody = Optional.ofNullable(method.getBody());
    return methodBody.isPresent() && !methodBody.get().getStatements().isEmpty();
  }

  private boolean methodHasTestAnnotation(CtMethod<?> method) {
    return (method.getAnnotations().stream()
            .anyMatch(a -> a.toString().contains("Test")));
  }

  private boolean areParametersPrimitivesOrStrings(CtInvocation<?> invocation) {
    // resolve unreported cases such as getLeftSideBearing() invocations in testPDFBox3319()
    return (invocation.getArguments().stream().allMatch(a -> a.getType().isPrimitive()
            || a.getType().getQualifiedName().equals("java.lang.String")))
            || (invocation.getExecutable().getParameters().stream().allMatch(p -> p.isPrimitive()
            || p.getQualifiedName().equals("java.lang.String")));
  }

  private List<String> getParametersAsPrimitivesOrStrings(CtInvocation<?> invocation) {
    List<String> parameterTypes = new ArrayList<>();
    // infer types from arguments directly if all primitives or Strings
    invocation.getArguments().forEach(a -> {
      if (a.getType().isPrimitive() || a.getType().getQualifiedName().equals("java.lang.String")) {
        parameterTypes.add(a.getType().getQualifiedName());
      }
    });
    // infer types from executable
    if (parameterTypes.isEmpty())
      invocation.getExecutable().getParameters().forEach(p -> parameterTypes.add(p.getQualifiedName()));
    return parameterTypes;
  }

  private boolean isInvocationOnJavaOrExternalLibraryMethod(CtInvocation<?> invocation) {
    List<String> typesToIgnore = List.of("java", "junit.framework",
            "org.mockito", "org.powermock", "org.testng");
    return typesToIgnore.stream().anyMatch(t -> invocation.getExecutable()
            .getDeclaringType().getQualifiedName().startsWith(t));
  }

  private List<InvocationWithPrimitiveParams> getMethodInvocationsWithPrimitiveParameters(CtMethod<?> testMethod) {
    List<InvocationWithPrimitiveParams> invocationsWithPrimitiveParams = new LinkedList<>();
    if (methodIsNotEmpty(testMethod)) {
      List<CtStatement> statements = testMethod.getBody().getStatements();
      for (CtStatement statement : statements) {
        List<CtInvocation<?>> invocationsInStatement = statement.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> invocation : invocationsInStatement) {
          if (!invocation.getArguments().isEmpty()
                  & !invocation.toString().toLowerCase().contains("assert")) {
            if (areParametersPrimitivesOrStrings(invocation) & !isInvocationOnJavaOrExternalLibraryMethod(invocation)) {
              InvocationWithPrimitiveParams thisInvocation = new InvocationWithPrimitiveParams(
                      invocation.prettyprint(),
                      invocation.getExecutable().getDeclaringType().getQualifiedName()
                              + "." + invocation.getExecutable().getSignature(),
                      invocation.getExecutable().getDeclaringType().getQualifiedName(),
                      invocation.getExecutable().getSimpleName(),
                      getParametersAsPrimitivesOrStrings(invocation),
                      invocation.getExecutable().getType().getQualifiedName());
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

  public Set<String> getSetOfTestClasses() {
    return setOfTestClasses;
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
      setOfTestClasses.add(method.getDeclaringType().getSimpleName());
    }
  }
}
