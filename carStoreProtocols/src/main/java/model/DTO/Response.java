package model.DTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public record Response(RESPONSE_CODE responseCode, String message) {

    @Override
    public String toString() {
        // Formata a saída do toString para combinar com o formato utilizado no fromString
        return String.format("Response{responseCode=%s, message='%s'}",
                responseCode.name(), message);
    }

    public static Response fromString(String str) {
        // Regex para extrair os valores dos campos com o formato correto
        Pattern pattern = Pattern.compile("Response\\{responseCode=([A-Z_]+), message='([^']*)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            RESPONSE_CODE responseCode = RESPONSE_CODE.valueOf(matcher.group(1));
            String message = matcher.group(2);
            return new Response(responseCode, message);
        }

        throw new IllegalArgumentException("Invalid string format");
    }
}