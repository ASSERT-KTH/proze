package se.kth.assrt.proze.sample;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultipleInvocationsWithinATest {

  @Test
  public void testMultipleInvocationsToDifferentMethods() {
    // this constructor invocation should be included
    SampleMethods sampleMethods = new SampleMethods("value");
    // this constructor invocation should not be included
    SampleMethods sampleMethods1 = new SampleMethods();
    // this invocation should be included
    int actualOne = sampleMethods.methodWithMultipleIntArgs(42, 42);
    // this invocation should be included
    String actualTwo = sampleMethods.methodWithSingleStringArg(
            "koko, the gorilla");
    // this invocation should not be included
    int actualThree = sampleMethods.methodWeWillNotSelectBecauseNonPrimitiveArg(
            List.of("some", "strings", "here"));
    sampleMethods.methodWithNoArgs();
    assertEquals(84, actualOne);
    assertEquals("KOKO, THE GORILLA", actualTwo);
    assertEquals(3, actualThree);
  }
}
