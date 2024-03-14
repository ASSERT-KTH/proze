package se.kth.assrt.proze.sample;

public class MethodRunner {
  public static void main(String[] args) {
    SampleMethods sampleMethods = new SampleMethods();
    System.out.println("methodWithSingleIntArg(42): "
            + sampleMethods.methodWithSingleIntArg(42));
    System.out.println("methodWithMultipleIntArgs(17, 27): "
            + sampleMethods.methodWithMultipleIntArgs(17, 27));
    System.out.println("methodWithSingleStringArg(\"magic\"): " +
            sampleMethods.methodWithSingleStringArg("magic"));
    System.out.println("methodWithMultipleStringArgs(\"magic\", \"faraway\"): "
            + sampleMethods.methodWithMultipleStringArgs("magic", "faraway"));
  }
}
