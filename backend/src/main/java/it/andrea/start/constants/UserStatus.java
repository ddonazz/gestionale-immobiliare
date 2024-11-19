package it.andrea.start.constants;

import java.util.stream.Stream;

public enum UserStatus {

    PENDING("PENDING"), 
    ACTIVE("ACTIVE"), 
    SUSPENDED("SUSPENDED"), 
    DEACTIVE("DEACTIVE"), 
    BLACKLIST("BLACKLIST");

    // Member to hold the name
    private String string;

    // constructor to set the string
    UserStatus(String name) {
        string = name;
    }

    @Override
    public String toString() {
        return string;
    }

    public static Stream<UserStatus> stream() {
        return Stream.of(UserStatus.values());
    }

}
