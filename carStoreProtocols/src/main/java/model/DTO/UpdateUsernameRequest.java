package model.DTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record UpdateUsernameRequest(String login, String newUsername) {

    @Override
    public String toString() {
        return String.format("UpdateUsernameRequest{login='%s', newUsername='%s'}",
                login, newUsername);
    }

    public static UpdateUsernameRequest fromString(String str) {
        Pattern pattern = Pattern.compile("UpdateUsernameRequest\\{login='([^']*)', newUsername='([^']*)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            String login = matcher.group(1);
            String newUsername = matcher.group(2);
            return new UpdateUsernameRequest(login, newUsername);
        }

        throw new IllegalArgumentException("Invalid string format");
    }
}
