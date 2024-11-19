package it.andrea.start.constants;

import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum Gender {
    
    MALE("MALE"),
    FEMALE("FEMALE"),
    NOT_SPECIFIED("NOT_SPECIFIED");

    // Member to hold the name
    private String string;

    // constructor to set the string
    Gender(String name) {
        string = name;
    }

    @Override
    public String toString() {
        return string;
    }

    public static Stream<Gender> stream() {
        return Stream.of(Gender.values());
    }

}
