package se.kth.assrt.proze.sample;

public class Book {
  private String title;
  private String author;
  private int quantity;

  public Book() {}

  public Book(String title, String author, int quantity) {
    this.title = title;
    this.author = author;
    this.quantity = quantity;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public boolean isAvailableToBorrow() {
    return this.quantity > 0;
  }
}
