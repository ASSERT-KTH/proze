package se.kth.assrt.proze.select;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MethodProcessorTest {
  static final String projectPath = "../sample/proze-sample";
  static MavenLauncher mavenLauncher;
  static CtModel model;
  static ProzeSelector prozeSelector;
  static final ProzeTestMethodProcessor prozeTestMethodProcessor
          = new ProzeTestMethodProcessor();

  @BeforeAll
  public static void setUpAndBuildModel() {
    prozeSelector = new ProzeSelector(Path.of(projectPath));
    assertDoesNotThrow(() -> {
              mavenLauncher = new MavenLauncher(projectPath,
                      MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
              mavenLauncher.buildModel();
              model = mavenLauncher.getModel();
              model.processWith(prozeTestMethodProcessor);
            },
            "Model building should work for proze-sample"
    );
  }

  @Test
  public void testThatTestMethodsAreFoundByProcessor() {
    assertEquals(20,
            prozeTestMethodProcessor.getTestMethods().size(),
            "20 public @Test methods should be found within proze-sample");
  }

  @Test
  public void testThatSetOfTestClassesIsFound() {
    assertEquals(19,
            prozeTestMethodProcessor.getSetOfTestClasses().size(),
            "19 test classes containing test methods " +
                    "with invocations should be found");
  }

  @Test
  public void testThatTestClassWithNoEligibleInvocationIsNotIncludedInSet() {
    assertTrue(prozeTestMethodProcessor.getSetOfTestClasses().stream().noneMatch(
                    c -> c.equals("OtherTest")),
            "OtherTest does not have a test method containing an candidate invocation," +
                    "it should not be included in the set of test classes");
  }

  @Test
  public void testInfoOnMethodWithMultipleIntArgs() {
    ProzeTestMethod firstTestMethod = prozeTestMethodProcessor.getTestMethods().stream()
            .filter(m -> m.getTestName().equals("testMethodWithMultipleIntArgs")).findFirst().get();
    assertEquals("se.kth.assrt.proze.sample.MultipleIntTest",
            firstTestMethod.getTestClassName());
    assertEquals("testMethodWithMultipleIntArgs",
            firstTestMethod.getTestName());
    List<InvocationWithPrimitiveParams> invocationsWithPrimitiveParams =
            firstTestMethod.getInvocationWithPrimitiveParams();
    assertEquals(1, invocationsWithPrimitiveParams.size());
    assertEquals("methodWithMultipleIntArgs",
            invocationsWithPrimitiveParams.get(0).getMethodName());
    assertEquals("se.kth.assrt.proze.sample.SampleMethods.methodWithMultipleIntArgs(int,int)",
            invocationsWithPrimitiveParams.get(0).getFullMethodSignature());
    List<String> parameterTypes = invocationsWithPrimitiveParams.get(0).getMethodParameterTypes();
    assertEquals(2, parameterTypes.size());
    for (String parameterType : parameterTypes) {
      assertEquals("int", parameterType);
    }
  }

  @Test
  public void testInfoOnTestMethodWithMultipleInvocationsToDifferentMethods() {
    Optional<ProzeTestMethod> testMethod = prozeTestMethodProcessor.getTestMethods().stream()
            .filter(m -> m.getTestName().equals("testMultipleInvocationsToDifferentMethods"))
            .findFirst();
    assertTrue(testMethod.isPresent());
    List<InvocationWithPrimitiveParams> invocationWithPrimitiveParams
            = testMethod.get().getInvocationWithPrimitiveParams();
    assertEquals(3, invocationWithPrimitiveParams.size());
    assertEquals("init",
            invocationWithPrimitiveParams.get(0).getMethodName());
    assertEquals("methodWithMultipleIntArgs",
            invocationWithPrimitiveParams.get(1).getMethodName());
    assertEquals("methodWithSingleStringArg",
            invocationWithPrimitiveParams.get(2).getMethodName());
  }

  @Test
  public void testInfoOnParameterizedConstructor() {
    Optional<ProzeTestMethod> testMethod = prozeTestMethodProcessor.getTestMethods().stream()
            .filter(m -> m.getTestName().equals("testBookCreationWithParameterizedConstructor"))
            .findFirst();
    assertTrue(testMethod.isPresent());
    List<InvocationWithPrimitiveParams> invocationWithPrimitiveParams =
            testMethod.get().getInvocationWithPrimitiveParams();
    assertEquals("init", invocationWithPrimitiveParams.get(0).getMethodName());
    assertEquals(3, invocationWithPrimitiveParams.get(0).getMethodParameterTypes().size());
    assertEquals(List.of("java.lang.String", "java.lang.String", "int"),
            invocationWithPrimitiveParams.get(0).getMethodParameterTypes());
  }

  @Test
  public void testThatTestWithoutAssertionsIsNotSelected() {
    assertTrue(prozeTestMethodProcessor.getTestMethods().stream()
            .noneMatch(m -> m.getTestName().equals("testWithoutAssertions")));
  }

  @Test
  public void testThatParameterizedTestIsNotSelected() {
    assertTrue(prozeTestMethodProcessor.getTestMethods().stream()
            .noneMatch(m -> m.getTestName().equals("testAParameterizedTest")));
  }
}