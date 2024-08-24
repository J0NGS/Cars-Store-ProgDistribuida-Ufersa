package model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@EqualsAndHashCode
public class Users implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String login;
    private String password;
    private USER_POLICY policy;

    public Users() {}

    public Users(UUID id, String login, String password, USER_POLICY policy) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.policy = policy;
    }

    @Override
    public String toString() {
        return String.format("Users{id=%s, login='%s', password='%s', policy=%s}",
                id, login, password, policy.name());
    }

    public static Users fromString(String str) {
        // Regex para extrair valores dos campos com o formato correto
        Pattern pattern = Pattern.compile("Users\\{id=([\\w-]+), login='([^']*)', password='([^']*)', policy=([A-Z_]+)\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            UUID id = UUID.fromString(matcher.group(1));
            String login = matcher.group(2);
            String password = matcher.group(3);
            USER_POLICY policy = USER_POLICY.valueOf(matcher.group(4));
            return new Users(id, login, password, policy);
        }

        throw new IllegalArgumentException("Invalid string format");
    }
}
