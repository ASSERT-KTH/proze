package se.kth.assrt.proze.sample;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultipleIntTest {
  static SampleMethods sampleMethods;

  @BeforeAll
  public static void setUp() {
    sampleMethods = new SampleMethods();
  }

  @Test
  public void testMethodWithMultipleIntArgs() {
    int actual = sampleMethods.methodWithMultipleIntArgs(6, 6);
    assertEquals(12, actual);
  }

  @Test
  public void testMethodWithSingleIntArgWithNegativeArg() {
    int actual = sampleMethods.methodWithMultipleIntArgs(-6, 6);
    assertEquals(0, actual);
  }

  @Test
  public void testMethodWithSingleIntArgWithZeroArg() {
    int actual = sampleMethods.methodWithMultipleIntArgs(0, 6);
    assertEquals(6, actual);
  }

}
