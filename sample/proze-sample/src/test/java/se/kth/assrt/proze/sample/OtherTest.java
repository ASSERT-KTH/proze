package se.kth.assrt.proze.sample;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OtherTest {

  @Test
  public void testMethodThatWeWillNotSelect() {
    SampleMethods sampleMethods = new SampleMethods();
    int actual = sampleMethods.methodWeWillNotSelectBecauseNonPrimitiveArg(
            List.of("could", "be", "anything"));
    assertEquals(3, actual);
  }
}
