package model.DTO;

import model.CAR_CATEGORY;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CreateCarRequest(String name, CAR_CATEGORY category, int year, double price, String renavam) {

    @Override
    public String toString() {
        // Formata a saída do toString para combinar com o formato utilizado no fromString
        return String.format("CreateCarRequest{name='%s', category=%s, year=%d, price=%s, renavam='%s'}",
                name, category.name(), year, formatPrice(price), renavam);
    }

    public static CreateCarRequest fromString(String str) {
        // Regex para extrair os valores dos campos com o formato correto
        Pattern pattern = Pattern.compile("CreateCarRequest\\{name='([^']*)', category=([A-Z_]+), year=(\\d+), price=([\\d.,]+), renavam='([^']*)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            String name = matcher.group(1);
            CAR_CATEGORY category = CAR_CATEGORY.valueOf(matcher.group(2));
            int year = Integer.parseInt(matcher.group(3));
            String priceStr = matcher.group(4).replace(',', '.'); // Substitui vírgula por ponto
            double price = Double.parseDouble(priceStr);
            String renavam = matcher.group(5);
            return new CreateCarRequest(name, category, year, price, renavam);
        }

        throw new IllegalArgumentException("Invalid string format");
    }

    private static String formatPrice(double price) {
        // Formata o preço para usar vírgula como separador decimal
        return String.format("%.2f", price).replace('.', ',');
    }
}
