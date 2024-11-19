package it.andrea.start.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelperQuery {

	private HelperQuery() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	private static final Logger LOG = LoggerFactory.getLogger(HelperQuery.class);

	public static String prepareForLikeQuery(String inputString) {
		if (inputString == null) {
			LOG.warn("Input string is null. Returning empty string.");
			return "";
		}
		return "%" + inputString.toUpperCase() + "%";
	}

}
