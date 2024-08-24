package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import java.util.logging.Logger;

public class UsersTest {

    private static final Logger logger = Logger.getLogger(UsersTest.class.getName());

    @Test
    public void testFromString() {
        Users user = new Users();
        user.setLogin("teste");
        user.setId(UUID.randomUUID());
        user.setPolicy(USER_POLICY.EMPLOYEE);
        user.setPassword("teste");
        String userString = user.toString();
        logger.info("Generated Users string: " + userString);
        Users userFromString = Users.fromString(userString);
        logger.info("Converted Users from string: " + userFromString);
        assertNotNull(userFromString, "User from string should not be null");
        assertEquals(user.getLogin(), userFromString.getLogin(), "Login should match");
        assertEquals(user.getId(), userFromString.getId(), "ID should match");
        assertEquals(user.getPolicy(), userFromString.getPolicy(), "Policy should match");
        assertEquals(user.getPassword(), userFromString.getPassword(), "Password should match");

    }
}
