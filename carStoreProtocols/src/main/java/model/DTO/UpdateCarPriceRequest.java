package model.DTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record UpdateCarPriceRequest(String renavam, double newPrice) {

    @Override
    public String toString() {
        return String.format("UpdateCarPriceRequest{renavam='%s', newPrice=%s}",
                renavam, formatPrice(newPrice));
    }

    public static UpdateCarPriceRequest fromString(String str) {
        Pattern pattern = Pattern.compile("UpdateCarPriceRequest\\{renavam='([^']*)', newPrice=([\\d.,]+)\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            String renavam = matcher.group(1);
            String priceStr = matcher.group(2).replace(',', '.'); // Substitui vírgula por ponto
            double newPrice = Double.parseDouble(priceStr);
            return new UpdateCarPriceRequest(renavam, newPrice);
        }

        throw new IllegalArgumentException("Invalid string format");
    }

    private static String formatPrice(double price) {
        return String.format("%.2f", price).replace('.', ',');
    }
}
