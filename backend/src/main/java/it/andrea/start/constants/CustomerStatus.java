package it.andrea.start.constants;

import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum CustomerStatus {

    PENDING("PENDING"), ACTIVE("ACTIVE"), SUSPENDED("SUSPENDED"), CANCELLED("CANCELLED"), BLACKLIST("BLACKLIST");

    // Member to hold the name
    private String string;

    // constructor to set the string
    CustomerStatus(String name) {
	string = name;
    }

    @Override
    public String toString() {
	return string;
    }

    public static Stream<CustomerStatus> stream() {
	return Stream.of(CustomerStatus.values());
    }

}
