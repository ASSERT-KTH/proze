package se.kth.assrt.proze.sample;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookTest {
  @Test
  public void testBookAvailability() {
    Book book = new Book();
    book.setTitle("lorem ipsum");
    book.setAuthor("Jane Doe");
    book.setQuantity(99999);
    assertTrue(book.isAvailableToBorrow(),
            "Book should be available to borrow if its quantity > 0");
  }

  @Test
  public void testBookUnavailability() {
    Book book = new Book();
    book.setTitle("dolor sit");
    book.setAuthor("John Doe");
    book.setQuantity(0);
    assertFalse(book.isAvailableToBorrow(),
            "Book should not be available to borrow if its quantity <= 0");
  }

  @Test
  public void testBookCreationWithParameterizedConstructor() {
    Book book = new Book("some book", "some author", 12345);
    assertTrue(book.isAvailableToBorrow(),
            "Book should be available to borrow if its quantity > 0");
  }
}
