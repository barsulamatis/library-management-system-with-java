package com.librarymanagementsystem.model;

import java.time.LocalDateTime;

public class Book {
    private int bookId;
    private String title;
    private String author;
    private String isbn;
    private int quantity;
    private int availableQuantity;
    private String category;
    private LocalDateTime dateAdded;
    private double price;
    private String description;

    public Book() {}

    public Book(String title, String author, String isbn, int quantity, int availableQuantity,
                String category, double price, String description) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.category = category;
        this.dateAdded = LocalDateTime.now();
        this.price = price;
        this.description = description;
    }

    // Getters and Setters
    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                ", category='" + category + '\'' +
                ", price=" + price +
                '}';
    }
}
