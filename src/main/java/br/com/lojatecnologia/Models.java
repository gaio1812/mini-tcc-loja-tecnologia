package br.com.lojatecnologia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Models {
    private Models() {}
    public record User(int id, String name, String email, String role) {}
    public record Customer(int id, String name, String cpf, String phone, String email, String address) {
        @Override public String toString() { return name + " — " + cpf; }
    }
    public record Product(int id, String name, String category, String brand, int stock, BigDecimal price) {
        @Override public String toString() { return name + " (estoque: " + stock + ")"; }
    }
    public record Sale(int id, String customer, LocalDateTime date, String payment, String status, BigDecimal total) {}
    public record SaleItem(String product, int quantity, BigDecimal price) {
        public BigDecimal total() { return price.multiply(BigDecimal.valueOf(quantity)); }
    }
    public record Summary(int customers, int products, int lowStock, BigDecimal revenue) {}
}
