package se.kth.assrt.proze.sample;

public class MethodRunner {
  public static void main(String[] args) {
    SampleMethods sampleMethods = new SampleMethods();
    SampleMethods sampleMethods2 = new SampleMethods("production-value");

    Book bookOne = new Book();
    bookOne.setTitle("Malgudi Days");
    bookOne.setAuthor("R K Narayan");
    bookOne.setQuantity(89);
    bookOne.isAvailableToBorrow();

    Book bookTwo = new Book("Karlsson på taket", "Astrid Lindgren", 167);
    bookTwo.isAvailableToBorrow();

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
