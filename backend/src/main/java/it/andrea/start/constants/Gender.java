package it.andrea.start.constants;

import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum Gender {

    MALE("Male"), FEMALE("Female"), NOT_SPECIFIED("Not specified");

    private String string;

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
