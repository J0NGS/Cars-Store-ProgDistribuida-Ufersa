package model.DTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Login(String username, String password) {

    @Override
    public String toString() {
        // Formata a saída do toString para combinar com o formato utilizado no fromString
        return String.format("Login{username='%s', password='%s'}", username, password);
    }

    public static Login fromString(String str) {
        // Regex para extrair os valores dos campos com o formato correto
        Pattern pattern = Pattern.compile("Login\\{username='([^']*)', password='([^']*)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            String username = matcher.group(1);
            String password = matcher.group(2);
            return new Login(username, password);
        }

        throw new IllegalArgumentException("Invalid string format");
    }
}
