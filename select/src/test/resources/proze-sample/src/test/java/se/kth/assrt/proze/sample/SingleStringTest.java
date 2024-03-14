package se.kth.assrt.proze.sample;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class SingleStringTest {
  static SampleMethods sampleMethods;

  @BeforeAll
  public static void setUp() {
    sampleMethods = new SampleMethods();
  }

  @Test
  public void testMethodWithSingleStringArg() {
    String actual = sampleMethods.methodWithSingleStringArg("hello");
    assertEquals("HELLO", actual);
  }

  @Test
  public void testMethodWithSingleStringArgEmptyString() {
    String actual = sampleMethods.methodWithSingleStringArg("");
    assertEquals("", actual);
  }

  @Test
  public void testMethodWithSingleStringArgWithNewline() {
    String actual = sampleMethods.methodWithSingleStringArg("New\nLine");
    assertEquals("NEW\nLINE", actual);
  }

  @Test
  public void testMethodWithSingleStringArgStringNull() {
    String actual = sampleMethods.methodWithSingleStringArg(null);
    assertNull(actual, "Cannot uppercase null");
  }
}
