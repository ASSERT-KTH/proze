package se.kth.assrt.proze.select;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

public class ProzeTestMethodProcessor extends AbstractProcessor<CtMethod<?>> {

  private final List<ProzeTestMethod> testMethods = new LinkedList<>();

  private final Set<String> setOfTestClasses = new LinkedHashSet<>();

  private boolean methodIsNotEmpty(CtMethod<?> method) {
    Optional<CtBlock<?>> methodBody = Optional.ofNullable(method.getBody());
    return methodBody.isPresent() && !methodBody.get().getStatements().isEmpty();
  }

  // Consider only @Test methods without annotation values
  // (such as @Test(dataProvider = "") in TestNG)
  private boolean methodHasTestAnnotation(CtMethod<?> method) {
    return (method.getAnnotations().stream()
            .anyMatch(a -> a.toString().contains(".Test")
            && a.getValues().isEmpty()));
  }

  private boolean methodHasAtLeastOneAssertion(CtMethod<?> method) {
    return methodIsNotEmpty(method) && method.getBody().getStatements().stream()
            .anyMatch(s -> s.toString().toLowerCase().contains("assert"));
  }

  private boolean areParametersPrimitivesOrStrings(CtAbstractInvocation<?> invocation) {
    // resolve unreported cases such as getLeftSideBearing() invocations in testPDFBox3319()
    return (invocation.getArguments().stream().allMatch(a -> a.getType().isPrimitive()
            || a.getType().getQualifiedName().equals("java.lang.String")))
            || (invocation.getExecutable().getParameters().stream().allMatch(p -> p.isPrimitive()
            || p.getQualifiedName().equals("java.lang.String")));
  }

  private List<String> getParametersAsPrimitivesOrStrings(CtAbstractInvocation<?> invocation) {
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

  private boolean isInvocationOnJavaOrExternalLibraryMethod(CtAbstractInvocation<?> invocation) {
    List<String> typesToIgnore = List.of("java", "junit.framework", "io.dropwizard",
            "org.apache.commons.lang", "org.junit", "org.hamcrest", "org.mockito", "org.powermock",
            "org.testng", "org.slf4j", "com.carrotsearch", "com.fasterxml");
    return typesToIgnore.stream().anyMatch(t -> invocation.getExecutable()
            .getDeclaringType().getQualifiedName().startsWith(t));
  }

  private String getInvocationLiterals(CtAbstractInvocation<?> invocation) {
    String invocationLiterals = "PROZE-NO-LITERALS";
    if (invocation.getArguments().stream()
            .allMatch(a -> a.getClass().getSimpleName().equals("CtLiteralImpl"))) {
      invocationLiterals = invocation.getArguments().stream()
              .map(a -> a.toString().replaceAll("\"", ""))
              .collect(Collectors.joining(","));

    }
    return invocationLiterals;
  }

  private List<InvocationWithPrimitiveParams> getConstructorInvocationsWithPrimitiveParams(CtStatement statement) {
    List<InvocationWithPrimitiveParams> constructorInvocationsWithPrimitiveParams = new ArrayList<>();
    List<CtConstructorCall<?>> constructorCalls =
            statement.getElements(new TypeFilter<>(CtConstructorCall.class));
    for (CtConstructorCall<?> constructorCall : constructorCalls) {
      if (!constructorCall.getArguments().isEmpty()
              & areParametersPrimitivesOrStrings(constructorCall)
              & !isInvocationOnJavaOrExternalLibraryMethod(constructorCall)) {
        List<String> constructorParameterTypes = getParametersAsPrimitivesOrStrings(constructorCall);
        String constructorParameterTypesStringified
                = constructorParameterTypes.toString().replaceAll("\\s", "")
                .replaceAll("\\[", "").replaceAll("]", "");
        String invocationLiterals = getInvocationLiterals(constructorCall);
        InvocationWithPrimitiveParams thisInvocation = new InvocationWithPrimitiveParams(
                constructorCall.prettyprint(),
                invocationLiterals,
                constructorCall.getExecutable().getDeclaringType().getQualifiedName()
                        + ".init(" + constructorParameterTypesStringified + ")",
                constructorCall.getExecutable().getDeclaringType().getQualifiedName(),
                "init",
                constructorParameterTypes,
                constructorCall.getExecutable().getType().getQualifiedName());
        constructorInvocationsWithPrimitiveParams.add(thisInvocation);
      }
    }
    return constructorInvocationsWithPrimitiveParams;
  }

  private List<InvocationWithPrimitiveParams> getMethodInvocationsWithPrimitiveParams(CtStatement statement) {
    List<InvocationWithPrimitiveParams> methodInvocationsWithPrimitiveParams = new ArrayList<>();
    List<CtInvocation<?>> invocationsInStatement = statement.getElements(new TypeFilter<>(CtInvocation.class));
    for (CtInvocation<?> invocation : invocationsInStatement) {
      if (!invocation.getArguments().isEmpty()
              & !invocation.toString().toLowerCase().contains("assert")) {
        if (areParametersPrimitivesOrStrings(invocation) & !isInvocationOnJavaOrExternalLibraryMethod(invocation)) {
          String invocationLiterals = getInvocationLiterals(invocation);
          InvocationWithPrimitiveParams thisInvocation = new InvocationWithPrimitiveParams(
                  invocation.prettyprint(),
                  invocationLiterals,
                  invocation.getExecutable().getDeclaringType().getQualifiedName()
                          + "." + invocation.getExecutable().getSignature(),
                  invocation.getExecutable().getDeclaringType().getQualifiedName(),
                  invocation.getExecutable().getSimpleName(),
                  getParametersAsPrimitivesOrStrings(invocation),
                  invocation.getExecutable().getType().getQualifiedName());
          methodInvocationsWithPrimitiveParams.add(thisInvocation);
        }
      }
    }
    return methodInvocationsWithPrimitiveParams;
  }

  private List<InvocationWithPrimitiveParams> getInvocationsWithPrimitiveParameters(CtMethod<?> testMethod) {
    List<InvocationWithPrimitiveParams> invocationsWithPrimitiveParams = new LinkedList<>();
    List<CtStatement> statements = testMethod.getBody().getStatements();
    for (CtStatement statement : statements) {
      invocationsWithPrimitiveParams.addAll(getMethodInvocationsWithPrimitiveParams(statement));
      invocationsWithPrimitiveParams.addAll(getConstructorInvocationsWithPrimitiveParams(statement));
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
            & methodHasTestAnnotation(method)
            & methodHasAtLeastOneAssertion(method)) {
      List<InvocationWithPrimitiveParams> invocationWithPrimitiveParams =
              getInvocationsWithPrimitiveParameters(method);
      ProzeTestMethod testMethod = new ProzeTestMethod(
              method.getDeclaringType().getQualifiedName(),
              method.getSimpleName(),
              method.getSignature(),
              invocationWithPrimitiveParams);
      testMethods.add(testMethod);
      // If there are candidate invocations within this test, get test class name
      if (!invocationWithPrimitiveParams.isEmpty()) {
        setOfTestClasses.add(method.getDeclaringType().getSimpleName() + "#" + method.getSimpleName());
      }
    }
  }
}
