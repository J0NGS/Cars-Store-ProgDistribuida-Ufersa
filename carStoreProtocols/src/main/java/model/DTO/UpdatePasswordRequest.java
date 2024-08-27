package model.DTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record UpdatePasswordRequest(String login, String newPassword) {

    @Override
    public String toString() {
        return String.format("UpdatePasswordRequest{login='%s', newPassword='%s'}",
                login, newPassword);
    }

    public static UpdatePasswordRequest fromString(String str) {
        Pattern pattern = Pattern.compile("UpdatePasswordRequest\\{login='([^']*)', newPassword='([^']*)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            String login = matcher.group(1);
            String newPassword = matcher.group(2);
            return new UpdatePasswordRequest(login, newPassword);
        }

        throw new IllegalArgumentException("Invalid string format");
    }
}
