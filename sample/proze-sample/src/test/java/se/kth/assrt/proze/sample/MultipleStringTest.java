package se.kth.assrt.proze.sample;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultipleStringTest {
  static SampleMethods sampleMethods;

  @BeforeAll
  public static void setUp() {
    sampleMethods = new SampleMethods();
  }

  @Test
  public void testMethodWithMultipleStringArgs() {
    int actual = sampleMethods.methodWithMultipleStringArgs("hello", "world");
    assertEquals(10, actual);
  }

  @Test
  public void testMethodWithMultipleStringArgsWithComma() {
    int actual = sampleMethods.methodWithMultipleStringArgs("hello,", "goodbye");
    assertEquals(13, actual);
  }

  @Test
  public void testMethodWithMultipleStringArgsOneEmpty() {
    int actualFromFirstInvocation = sampleMethods.methodWithMultipleStringArgs("hello", "");
    assertEquals(5, actualFromFirstInvocation);
    int actualFromSecondInvocation = sampleMethods.methodWithMultipleStringArgs("", "world");
    assertEquals(5, actualFromSecondInvocation);

  }

  @Test
  public void testMethodWithMultipleStringArgsOneNull() {
    int actual = sampleMethods.methodWithMultipleStringArgs("hello", null);
    assertEquals(0, actual);
  }

  @Test
  public void testMethodWithMultipleStringArgsBothNull() {
    int actual = sampleMethods.methodWithMultipleStringArgs(null, null);
    assertEquals(0, actual);
  }
}
