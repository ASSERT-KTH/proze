package se.kth.assrt.proze.sample;

import java.util.List;

public class SampleMethods {
  String someField;

  public SampleMethods() {}

  public SampleMethods(String field) {
    this.someField = field;
  }

  public void methodWithNoArgs() {
    System.out.println("Nothing to do here");
  }

  public int methodWeWillNotSelectBecauseNonPrimitiveArg(List<String> stringList) {
    System.out.println("This method accepts a List of String arguments " +
            "so we will not select it");
    return stringList.size();
  }

  public String methodWithSingleStringArg(String stringArgument) {
    System.out.println("This method accepts a String and " +
            "returns it in uppercase");
    try {
      return stringArgument.toUpperCase();
    } catch (NullPointerException npe) {
      System.out.println("String was null, returning null");
      return null;
    }
  }

  public int methodWithMultipleStringArgs(String argOne, String argTwo) {
    System.out.println("This method accepts two String args and " +
            "returns the sum of their lengths");
    try {
      return argOne.length() + argTwo.length();
    } catch (NullPointerException npe) {
      System.out.println("(At least one) String was null");
      return 0;
    }
  }

  public int methodWithSingleIntArg(int intArg) {
    System.out.println("This method accepts an int and returns it + 42");
    return intArg + 42;
  }

  public int methodWithMultipleIntArgs(int argOne, int argTwo) {
    System.out.println("This method accepts two ints and returns their sum");
    return argOne + argTwo;
  }
}
