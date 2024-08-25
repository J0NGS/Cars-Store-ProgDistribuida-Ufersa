package model.DTO;

import model.USER_POLICY;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record UserResponse(UUID uuid, String login, USER_POLICY policy) {

    @Override
    public String toString() {
        // Formata a saída do toString para combinar com o formato utilizado no fromString
        return String.format("UserResponse{uuid=%s, login='%s', policy=%s}",
                uuid.toString(), login, policy.name());
    }

    public static UserResponse fromString(String str) {
        // Regex para extrair os valores dos campos com o formato correto
        Pattern pattern = Pattern.compile("UserResponse\\{uuid=([a-fA-F0-9\\-]+), login='([^']*)', policy=([A-Z_]+)\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            UUID uuid = UUID.fromString(matcher.group(1));
            String login = matcher.group(2);
            USER_POLICY policy = USER_POLICY.valueOf(matcher.group(3));
            return new UserResponse(uuid, login, policy);
        }

        throw new IllegalArgumentException("Invalid string format");
    }
}
