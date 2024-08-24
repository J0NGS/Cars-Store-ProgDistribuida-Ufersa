package model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        return String.format("Cars{id=%s, name='%s', category=%s, yearOfManufacture=%d, price=%.2f, renavam='%s'}",
                id, name, category, yearOfManufacture, price, renavam);
    }

    public static Cars fromString(String str) {
        Pattern pattern = Pattern.compile("Cars\\{id=([\\w-]+), name='([^']*)', category=([A-Z_]+), yearOfManufacture=(\\d+), price=(\\d+\\,\\d+), renavam='([^']*)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            UUID id = UUID.fromString(matcher.group(1));
            String name = matcher.group(2);
            CAR_CATEGORY category = CAR_CATEGORY.valueOf(matcher.group(3));
            int yearOfManufacture = Integer.parseInt(matcher.group(4));
            String priceStr = matcher.group(5).replace(',', '.');
            double price = Double.parseDouble(priceStr);
            String renavam = matcher.group(6);
            return new Cars(id, name, category, yearOfManufacture, price, renavam);
        }

        throw new IllegalArgumentException("Invalid string format");
    }

}


