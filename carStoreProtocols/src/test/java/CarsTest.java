import model.CAR_CATEGORY;
import model.Cars;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import java.util.logging.Logger;

public class CarsTest {

    private static final Logger logger = Logger.getLogger(CarsTest.class.getName());

    @Test
    public void testFromString() {
        // Prepare the test data
        Cars car = new Cars();
        car.setId(UUID.randomUUID());
        car.setName("HB20");
        car.setCategory(CAR_CATEGORY.ECONOMIC);
        car.setRenavam("09102931409123");
        car.setYearOfManufacture(2019);
        car.setPrice(80000);
        String carString = car.toString();

        // Log the generated string
        logger.info("Generated Cars string: " + carString);

        // Perform the conversion
        Cars carFromString = Cars.fromString(carString);

        // Log the results
        logger.info("Converted Cars from string: " + carFromString);

        // Verify the results
        assertNotNull(carFromString, "Car from string should not be null");
        assertEquals(car.getId(), carFromString.getId(), "ID should match");
        assertEquals(car.getName(), carFromString.getName(), "Name should match");
        assertEquals(car.getCategory(), carFromString.getCategory(), "Category should match");
        assertEquals(car.getYearOfManufacture(), carFromString.getYearOfManufacture(), "Year of Manufacture should match");
        assertEquals(car.getPrice(), carFromString.getPrice(), "Price should match");
        assertEquals(car.getRenavam(), carFromString.getRenavam(), "Renavam should match");
    }
}
