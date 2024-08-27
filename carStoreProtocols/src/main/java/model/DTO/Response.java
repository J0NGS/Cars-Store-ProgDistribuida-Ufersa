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
        // Primeiro, encontramos o índice de separação entre responseCode e message
        int responseCodeStart = str.indexOf("responseCode=") + "responseCode=".length();
        int messageStart = str.indexOf(", message='") + ", message='".length();

        if (responseCodeStart == -1 || messageStart == -1) {
            throw new IllegalArgumentException("Invalid string format");
        }

        // Extrair responseCode e message
        String responseCodeStr = str.substring(responseCodeStart, str.indexOf(',', responseCodeStart)).trim();
        String messageStr = str.substring(messageStart, str.lastIndexOf("'")).trim();

        // Converter responseCode para enum
        RESPONSE_CODE responseCode = RESPONSE_CODE.valueOf(responseCodeStr);

        return new Response(responseCode, messageStr);
    }

}