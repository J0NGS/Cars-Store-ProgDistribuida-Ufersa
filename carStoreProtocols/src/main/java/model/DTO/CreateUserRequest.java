package model.DTO;

import model.USER_POLICY;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CreateUserRequest(String login, String password, USER_POLICY policy) {

    @Override
    public String toString() {
        // Formata a saída do toString para combinar com o formato utilizado no fromString
        return String.format("CreateUserRequest{login='%s', password='%s', policy=%s}",
                login, password, policy.name());
    }

    public static CreateUserRequest fromString(String str) {
        // Regex para extrair os valores dos campos com o formato correto
        Pattern pattern = Pattern.compile("CreateUserRequest\\{login='([^']*)', password='([^']*)', policy=([A-Z_]+)\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            String login = matcher.group(1);
            String password = matcher.group(2);
            USER_POLICY policy = USER_POLICY.valueOf(matcher.group(3));
            return new CreateUserRequest(login, password, policy);
        }

        throw new IllegalArgumentException("Invalid string format");
    }
}