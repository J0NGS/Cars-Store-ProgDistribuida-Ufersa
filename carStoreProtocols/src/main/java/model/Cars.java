package model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class Cars implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String name;
    private CAR_CATEGORY category;
    private int yearOfManufacture;
    private double price;
    private String renavam;

    @Override
    public String toString() {
        return String.format("Cars{id=%s, name='%s', category=%s, yearOfManufacture=%d, price=%s, renavam='%s'}",
                id, name, category.name(), yearOfManufacture, formatPrice(price), renavam);
    }

    public static Cars fromString(String str) {
        // Regex para extrair os valores dos campos com o formato correto
        Pattern pattern = Pattern.compile("Cars\\{id=([\\w-]+), name='([^']*)', category=([A-Z_]+), yearOfManufacture=(\\d+), price=([\\d.,]+), renavam='([^']*)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            UUID id = UUID.fromString(matcher.group(1));
            String name = matcher.group(2);
            CAR_CATEGORY category = CAR_CATEGORY.valueOf(matcher.group(3));
            int yearOfManufacture = Integer.parseInt(matcher.group(4));
            String priceStr = matcher.group(5).replace(',', '.'); // Substitui vírgula por ponto
            double price = Double.parseDouble(priceStr);
            String renavam = matcher.group(6);
            return new Cars(id, name, category, yearOfManufacture, price, renavam);
        }

        throw new IllegalArgumentException("Invalid string format");
    }

    private static String formatPrice(double price) {
        // Formata o preço para usar vírgula como separador decimal
        return String.format("%.2f", price).replace('.', ',');
    }
}
