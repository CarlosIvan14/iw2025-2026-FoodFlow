package pos.domain;

public record Product(Long id, String name, String category, double price, boolean available) {}
