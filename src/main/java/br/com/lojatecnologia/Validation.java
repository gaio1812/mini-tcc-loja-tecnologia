package br.com.lojatecnologia;

import java.math.*;
import java.util.Locale;

final class Validation {
    private Validation() {}
    static String text(String value, String label, int max, boolean required) {
        String clean = value == null ? "" : value.trim();
        if (required && clean.isEmpty()) throw new IllegalArgumentException("Preencha " + label + ".");
        if (clean.length() > max) throw new IllegalArgumentException(label + ": limite de " + max + " caracteres.");
        return clean;
    }
    static String email(String value, boolean required) {
        String clean = text(value, "o e-mail", 100, required).toLowerCase(Locale.ROOT);
        if (!clean.isEmpty() && !clean.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Digite um e-mail válido.");
        }
        return clean;
    }
    static String cpf(String value) {
        String clean = value == null ? "" : value.replaceAll("[.\\-\\s]", "");
        if (!clean.matches("\\d{11}") || clean.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("Digite um CPF válido com 11 dígitos.");
        }
        for (int end = 9; end <= 10; end++) {
            int sum = 0;
            for (int i = 0; i < end; i++) sum += (clean.charAt(i) - '0') * (end + 1 - i);
            int digit = 11 - sum % 11;
            if (digit >= 10) digit = 0;
            if (digit != clean.charAt(end) - '0') throw new IllegalArgumentException("O CPF informado é inválido.");
        }
        return clean.substring(0,3) + "." + clean.substring(3,6) + "." + clean.substring(6,9) + "-" + clean.substring(9);
    }
    static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("O valor deve ficar entre 0,00 e 99.999.999,99.");
        }
        try { return value.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("Use no máximo duas casas decimais."); }
    }
}
