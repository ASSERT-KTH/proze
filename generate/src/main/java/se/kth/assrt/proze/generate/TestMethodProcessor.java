package se.kth.assrt.proze.generate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import spoon.processing.AbstractProcessor;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestMethodProcessor extends AbstractProcessor<CtMethod<?>> {
  private static final Logger logger = LoggerFactory.getLogger(TestMethodProcessor.class);
  List<TargetMethod> targetMethods;
  private final CtModel model;
  static TargetMethod currentTargetMethod;
  static final String generatorMethodName = "provideProzeArguments";
  // to prevent the same constructor call from being handled multiple times
  static Set<String> processedMethodSignatures = new LinkedHashSet<>();
  private static boolean isTestNG = false;

  public TestMethodProcessor(List<TargetMethod> targetMethods, CtModel model) {
    this.targetMethods = targetMethods;
    this.model = model;
  }

  public void replaceOriginalConstructorArgumentsWithArgsFromUnion(CtType<?> generatedClass) {
    CtMethod<?> testMethod = generatedClass.getMethods().stream()
            .filter(m -> m.getAnnotations().stream()
                    .anyMatch(a -> a.toString().contains("ParameterizedTest"))
                    || m.getAnnotations().stream()
                    .anyMatch(a -> a.toString().contains("Test(dataProvider")))
            .findFirst().get();
    List<CtConstructorCall<?>> constructorCalls = testMethod.getBody()
            .getElements(new TypeFilter<>(CtConstructorCall.class));
    for (CtConstructorCall<?> constructorCall : constructorCalls) {
      if (currentTargetMethod.getDeclaringType()
              .equals(constructorCall.getExecutable().getDeclaringType().getQualifiedName())
              & currentTargetMethod.getParameters().toString().equals(
              constructorCall.getExecutable().getParameters().toString())) {
        List<CtExpression<?>> params = new ArrayList<>();
        for (int i = 0; i < currentTargetMethod.getParameters().size(); i++) {
          params.add(generatedClass.getFactory()
                  .createCodeSnippetExpression("param" + i));
        }
        constructorCall.setArguments(params);
      }
    }
  }

  public void replaceOriginalMethodArgumentsWithArgsFromUnion(CtType<?> generatedClass) {
    CtMethod<?> testMethod = generatedClass.getMethods().stream()
            .filter(m -> m.getAnnotations().stream()
                    .anyMatch(a -> a.toString().contains("ParameterizedTest"))
                    || m.getAnnotations().stream()
                    .anyMatch(a -> a.toString().contains("Test(dataProvider")))
            .findFirst().get();
    List<CtInvocation<?>> invocations = testMethod.getBody()
            .getElements(new TypeFilter<>(CtInvocation.class));
    for (CtInvocation<?> invocation : invocations) {
      if (currentTargetMethod.getFullMethodSignature()
              .equals(invocation.getExecutable().getDeclaringType().getQualifiedName()
                      + "." + invocation.getExecutable().getSignature())) {
        List<CtExpression<?>> params = new ArrayList<>();
        for (int i = 0; i < currentTargetMethod.getParameters().size(); i++) {
          params.add(generatedClass.getFactory()
                  .createCodeSnippetExpression("param" + i));
        }
        invocation.setArguments(params);
      }
    }
  }

  public CtMethod<?> generateStaticParameterGeneratorMethod(Factory factory) {
    if (isTestNG) {
      return generateDataProviderTestNG(factory);
    } else {
      return generateStaticParameterGeneratorJUnit(factory);
    }
  }

  public CtMethod<?> generateDataProviderTestNG(Factory factory) {
    CtMethod<?> generatorMethod = factory.createMethod().setSimpleName("values");
    // make method private and static
    generatorMethod.setModifiers(Set.of(ModifierKind.PRIVATE, ModifierKind.STATIC));
    // add testng data provider annotation
    CtAnnotation<?> dataProviderAnnotation = factory.createAnnotation(
            factory.createCtTypeReference(DataProvider.class));
    dataProviderAnnotation.addValue("name", generatorMethodName);
    generatorMethod.addAnnotation(dataProviderAnnotation);
    // set type as Object[][]
    CtTypeReference<?> streamCtTypeReference = factory.createCtTypeReference(java.lang.Object[][].class);
    generatorMethod.setType(streamCtTypeReference);
    // body
    CtBlock<?> methodBody = factory.createBlock();
    StringBuilder arguments = buildArguments(currentTargetMethod.getUnionProdAndTestArgs(),
            "new java.lang.Object[]{", "}");
    methodBody.addStatement(factory.createCodeSnippetStatement(String.format(
            "return new java.lang.Object[][]{\n%s\n}",
            arguments)));
    generatorMethod.setBody(methodBody);
    return generatorMethod;
  }

  public CtMethod<?> generateStaticParameterGeneratorJUnit(Factory factory) {
    CtMethod<?> generatorMethod = factory.createMethod().setSimpleName(generatorMethodName);
    // make method private and static
    generatorMethod.setModifiers(Set.of(ModifierKind.PRIVATE, ModifierKind.STATIC));
    // set type as Stream<Arguments>
    CtTypeReference<?> streamCtTypeReference = factory.createCtTypeReference(Stream.class);
    streamCtTypeReference.setActualTypeArguments(List.of(factory
            .createCtTypeReference(Arguments.class)));
    generatorMethod.setType(streamCtTypeReference);
    // body
    CtBlock<?> methodBody = factory.createBlock();
    StringBuilder arguments = buildArguments(currentTargetMethod.getUnionProdAndTestArgs(),
            "org.junit.jupiter.params.provider.Arguments.of(", ")");
    methodBody.addStatement(factory.createCodeSnippetStatement(String.format(
            "return java.util.stream.Stream.of(\n%s\n)",
            arguments)));
    generatorMethod.setBody(methodBody);
    return generatorMethod;
  }

  private StringBuilder buildArguments(List<String> argsList, String returnTypeStart, String returnTypeEnd) {
    StringBuilder arguments = new StringBuilder();
    for (int i = 0; i < argsList.size(); i++) {
      String[] args = argsList.get(i).split(",");
      for (int j = 0; j < currentTargetMethod.getParameters().size(); j++) {
        if (currentTargetMethod.getParameters().get(j).equals("java.lang.String")) {
          if (args[j].equals("PROZE-NULL-STRING"))
            args[j] = null;
          else {
            // escape all " within this arg
            args[j] = args[j].replaceAll("\"", "\\\\\"");
            // remove all \n within this arg
            args[j] = args[j].replaceAll("\n", "");
            // enclose string within quotes => "arg"
            args[j] = "\"" + args[j] + "\"";
            // put back removed commas
            args[j] = args[j].replaceAll("PROZE-REDACTED-COMMA", ",");
            // put back empty strings
            args[j] = args[j].replaceAll("PROZE-EMPTY-STRING", "");
          }
        } else if (currentTargetMethod.getParameters().get(j).equals("boolean")) {
            args[j] = args[j].toLowerCase();
        }
      }
      String argsAsString = Arrays.toString(args).substring(1);
      argsAsString = argsAsString.substring(0, argsAsString.length() - 1);
      arguments.append(returnTypeStart)
              .append(argsAsString).append(returnTypeEnd);
      if (i < currentTargetMethod.getUnionProdAndTestArgs().size() - 1) {
        arguments.append(",\n");
      }
    }
    return arguments;
  }

  public CtType<?> generateOneParamTestPerAssertion(CtType<?> generatedClass) {
    for (CtMethod<?> method : generatedClass.getMethods()) {
      if (method.getAnnotations().stream()
              .anyMatch(a -> a.toString().contains("ParameterizedTest"))
              || method.getAnnotations().stream()
              .anyMatch(a -> a.toString().contains("Test(dataProvider"))) {
        // get the number of assert statements
        List<CtStatement> assertStatements = method.getBody().getStatements()
                .stream().filter(s -> s.toString().contains("assert"))
                .collect(Collectors.toList());
        logger.info("Found " + assertStatements.size() + " assertion statements");
        // if fewer than 2 assertions, nothing to do
        if (assertStatements.size() < 2)
          return generatedClass;
        // make a copy of the test for each assert statement
        for (int i = 0; i < assertStatements.size(); i++) {
          CtMethod<?> testCopy = generatedClass.getFactory().createMethod();
          testCopy.setType(method.getType());
          testCopy.setAnnotations(method.getAnnotations());
          testCopy.setSimpleName(method.getSimpleName() + "_" + (i + 1));
          testCopy.setThrownTypes(method.getThrownTypes());
          testCopy.setModifiers(method.getModifiers());
          testCopy.setParameters(method.getParameters());

          CtBlock<?> methodBody = generatedClass.getFactory().createBlock();
          // copy each statement, comment out other assert statements
          for (CtStatement statementToCopy : method.getBody().getStatements()) {
            List<CtElement> comments = statementToCopy.getDirectChildren().stream()
                    .filter(e -> e.getRoleInParent().toString().equals("comment"))
                    .collect(Collectors.toList());
            for (CtElement comment : comments) {
              statementToCopy.removeComment((CtComment) comment);
            }
            if (statementToCopy.toString().contains("assert")
                    && !statementToCopy.toString().equals(assertStatements.get(i).toString())) {
              CtStatement commentedOutStatement = generatedClass.getFactory()
                      .createCodeSnippetStatement(String.format("// %s", statementToCopy));
              methodBody.addStatement(commentedOutStatement);
            } else {
              methodBody.addStatement(statementToCopy);
            }
          }
          testCopy.setBody(methodBody);
          generatedClass.addMethod(testCopy);
        }
      }
    }
    return generatedClass;
  }

  public void updateBeforeAfterAnnotations(CtType<?> copyOfTestClass) {
    Map<String, Class<?>> annotations = Map.of(
            ".BeforeClass", BeforeAll.class,
            ".Before", BeforeEach.class,
            ".AfterClass", AfterAll.class,
            ".After", AfterEach.class);
    Factory factory = copyOfTestClass.getFactory();
    for (Map.Entry<String, Class<?>> annotationMap : annotations.entrySet()) {
      for (CtMethod<?> annotatedMethod : copyOfTestClass.getMethods().stream()
              .filter(m -> m.getAnnotations().stream()
                      .anyMatch(a -> a.toString().endsWith(annotationMap.getKey())))
              .collect(Collectors.toList())) {
        CtAnnotation<?> beforeClass = annotatedMethod.getAnnotations().stream()
                .filter(a -> a.toString().endsWith(annotationMap.getKey())).findFirst().get();
        annotatedMethod.removeAnnotation(beforeClass);
        annotatedMethod.addAnnotation(factory.createAnnotation(
                factory.createCtTypeReference(annotationMap.getValue())));
        if (annotationMap.getKey().endsWith("Class"))
          annotatedMethod.addModifier(ModifierKind.STATIC);
      }
    }
  }

  public CtType<?> copyTestClassAndPrepareTestMethod(CtType<?> testClassToCopy,
                                                     String testMethodToCopy,
                                                     String classNameSuffix) {
    CtType<?> copyOfTestClass = testClassToCopy.clone()
            .setSimpleName("TestProzeGen" + classNameSuffix);
    copyOfTestClass.accept(new CtScanner() {
      @Override
      public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
        super.visitCtTypeReference(reference);
        if (reference.getQualifiedName().equals(
                testClassToCopy.getQualifiedName())) {
          reference.setSimpleName(copyOfTestClass.getSimpleName());
        }
      }
    });
    // remove other @Test methods, not other methods such as @BeforeTest (testng)
    for (CtMethod<?> testMethod : copyOfTestClass.getMethods().stream()
            .filter(m -> m.getAnnotations().stream()
                    .anyMatch(a -> a.toString().contains(".Test") ||
                            a.toString().contains(".ParameterizedTest")))
            .collect(Collectors.toList())) {
      if (!testMethod.getSimpleName().equals(testMethodToCopy))
        copyOfTestClass.removeMethod(testMethod);
    }
    CtMethod<?> targetTestMethod = copyOfTestClass.getMethodsByName(
            testMethodToCopy).get(0);
    Factory factory = copyOfTestClass.getFactory();

    // remove @Test
    CtAnnotation<?> testAnnotation = targetTestMethod.getAnnotations().stream()
            .filter(a -> a.toString().contains("Test")).findFirst().get();
    targetTestMethod.removeAnnotation(testAnnotation);
    if (testAnnotation.toString().contains("testng.annotations.Test"))
      isTestNG = true;
    if (isTestNG) {
      // add @Test with datasource annotation
      CtAnnotation<?> methodSourceAnnotation = factory.createAnnotation(
              factory.createCtTypeReference(Test.class));
      methodSourceAnnotation.addValue("dataProvider", generatorMethodName);
      targetTestMethod.addAnnotation(methodSourceAnnotation);
    } else {
      // BeforeClass => BeforeAll
      // Before => BeforeEach
      // AfterClass => AfterAll
      // After => AfterEach
      updateBeforeAfterAnnotations(copyOfTestClass);

      // add @ParameterizedTest and @MethodSource annotations
      targetTestMethod.addAnnotation(factory.createAnnotation(
              factory.createCtTypeReference(ParameterizedTest.class)));
      CtAnnotation<?> methodSourceAnnotation = factory.createAnnotation(
              factory.createCtTypeReference(MethodSource.class));
      methodSourceAnnotation.addValue("value", generatorMethodName);
      targetTestMethod.addAnnotation(methodSourceAnnotation);
    }
    // add parameters
    for (int i = 0; i < currentTargetMethod.getParameters().size(); i++) {
      CtParameter<?> parameter = factory.createParameter()
              .setType(factory.createReference(currentTargetMethod
                      .getParameters().get(i)));
      parameter.setSimpleName("param" + i);
      targetTestMethod.addParameter(parameter);
    }
    // add generated class to the same package
    testClassToCopy.getPackage().addType(copyOfTestClass);
    return copyOfTestClass;
  }

  private List<CtType<?>> copyTestClassesWithInvokingTests(CtMethod<?> method) {
    List<CtType<?>> generatedTestClasses = new ArrayList<>();
    LinkedHashMap<String, Set<String>> testClassMethodMap = new LinkedHashMap<>();
    Set<String> testsThatInvokeDirectly = currentTargetMethod.getTestsThatInvokeDirectly();
    for (String test : testsThatInvokeDirectly) {
      // extract fqn of test class from test method signature
      String testClassName = test.replaceFirst("(.+)\\.([^.]*)$", "$1");
      String testMethodName = test.replaceFirst("(.+)\\.([^.]*)$", "$2");
      testClassMethodMap.computeIfAbsent(testClassName,
              v -> new LinkedHashSet<>()).add(testMethodName);
    }
    // maybe invoked in multiple tests within same test class: we will make one copy for each method
    logger.info("method invoked within " +
            testClassMethodMap.keySet().size() + " different classes");
    for (String testClass : testClassMethodMap.keySet()) {
      logger.info("testClass " + testClass);
      Optional<CtType<?>> foundTestClass = model.getAllTypes().stream()
              .filter(t -> testClass.equals(t.getQualifiedName())).findFirst();
      if (foundTestClass.isPresent()) {
        for (String testMethod : testClassMethodMap.get(testClass)) {
          logger.info("testMethod " + testMethod);
          CtType<?> thisTestClass = foundTestClass.get();
          String classNameSuffix = "_" + method.getDeclaringType().getSimpleName()
                  + "_" + (currentTargetMethod.getMethodName().equals("init") ? "init" : method.getSignature())
                  .replaceAll("\\(", "_")
                  .replaceAll("\\.", "_")
                  .replaceAll(",", "_")
                  .replaceAll("\\)", "")
                  + "_" + thisTestClass.getSimpleName()
                  + "_" + testMethod;
          CtType<?> newClass = copyTestClassAndPrepareTestMethod(thisTestClass,
                  testMethod, classNameSuffix);
          newClass = generateOneParamTestPerAssertion(newClass);
          logger.info("Generated class " + newClass.getQualifiedName());
          generatedTestClasses.add(newClass);
        }
      }
    }
    return generatedTestClasses;
  }

  @Override
  public void process(CtMethod<?> method) {
    String fullMethodSignature = method.getDeclaringType().getQualifiedName()
            + "." + method.getSignature();
    for (TargetMethod targetMethod : targetMethods) {
      // for constructor invocations
      if (targetMethod.getMethodName().equals("init")) {
        if (processedMethodSignatures.contains(targetMethod.getFullMethodSignature()))
          continue;
        if (method.getDeclaringType().getQualifiedName().equals(targetMethod.getDeclaringType())) {
          currentTargetMethod = targetMethod;
          logger.info("Working on constructor " + targetMethod.getFullMethodSignature());
          List<CtType<?>> generatedClasses = copyTestClassesWithInvokingTests(method);
          for (CtType<?> generatedClass : generatedClasses) {
            // generate a static generator method, generateForMethod
            generatedClass.addMethod(generateStaticParameterGeneratorMethod(
                    generatedClass.getFactory()));
            // replace parameter with call to generator
            replaceOriginalConstructorArgumentsWithArgsFromUnion(generatedClass);
          }
          processedMethodSignatures.add(currentTargetMethod.getFullMethodSignature());
          currentTargetMethod = null; // reset
        }
      }
      // for method invocations
      else if (targetMethod.getFullMethodSignature().equals(fullMethodSignature)) {
        currentTargetMethod = targetMethod;
        logger.info("Working on method " + fullMethodSignature);
        List<CtType<?>> generatedClasses = copyTestClassesWithInvokingTests(method);
        for (CtType<?> generatedClass : generatedClasses) {
          // generate a static generator method, generateForMethod
          generatedClass.addMethod(generateStaticParameterGeneratorMethod(
                  generatedClass.getFactory()));
          // replace parameter with call to generator
          replaceOriginalMethodArgumentsWithArgsFromUnion(generatedClass);
        }
        processedMethodSignatures.add(currentTargetMethod.getFullMethodSignature());
        currentTargetMethod = null;
        break;
      }
    }
  }
}
