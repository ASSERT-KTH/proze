package se.kth.assrt.proze.sample;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SingleIntTest {
  static SampleMethods sampleMethods;

  @BeforeAll
  public static void setUp() {
    sampleMethods = new SampleMethods();
  }

  @Test
  public void testMethodWithSingleIntArg() {
    int actual = sampleMethods.methodWithSingleIntArg(6);
    assertEquals(48, actual);
  }

  @Test
  public void testMethodWithSingleIntArgWithZeroArg() {
    int actual = sampleMethods.methodWithSingleIntArg(0);
    assertEquals(42, actual);
  }

  @Test
  public void testMethodWithSingleIntArgWithNegativeArg() {
    int actual = sampleMethods.methodWithSingleIntArg(-42);
    assertEquals(0, actual);
  }

}
