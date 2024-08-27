package model.DTO;

import model.CAR_CATEGORY;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record UpdateCarRequest(String renavam, String newName, CAR_CATEGORY newCategory, int newYear, double newPrice) {

    @Override
    public String toString() {
        return String.format("UpdateCarRequest{renavam='%s', newName='%s', newCategory=%s, newYear=%d, newPrice=%s}",
                renavam, newName, newCategory.name(), newYear, formatPrice(newPrice));
    }

    public static UpdateCarRequest fromString(String str) {
        Pattern pattern = Pattern.compile("UpdateCarRequest\\{renavam='([^']*)', newName='([^']*)', newCategory=([A-Z_]+), newYear=(\\d+), newPrice=([\\d.,]+)\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            String renavam = matcher.group(1);
            String newName = matcher.group(2);
            CAR_CATEGORY newCategory = CAR_CATEGORY.valueOf(matcher.group(3));
            int newYear = Integer.parseInt(matcher.group(4));
            String priceStr = matcher.group(5).replace(',', '.'); // Substitui vírgula por ponto
            double newPrice = Double.parseDouble(priceStr);
            return new UpdateCarRequest(renavam, newName, newCategory, newYear, newPrice);
        }

        throw new IllegalArgumentException("Invalid string format");
    }

    private static String formatPrice(double price) {
        return String.format("%.2f", price).replace('.', ',');
    }
}

