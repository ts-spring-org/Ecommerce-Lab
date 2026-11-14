package ms.identity.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

	// Regex domain extension accept point and +tag
	private static final Pattern EMAIL_PATTERN = Pattern
			.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+" +
			        "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
			        "@" +
			        "(?:[A-Za-z0-9]" +
			        "(?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+" +
			        "[A-Za-z]{2,63}$");

	public EmailAddress {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Email cannot be null or blank");
		}

		//1. Trim surrounding whitespace and normalize case.
		final String normalized = value.trim().toLowerCase(Locale.ROOT);

		// 2. Validation: length validation (RFC 5321 max length is 254)
		if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
			throw new IllegalArgumentException("Email address format is invalid");
		}

		int atIndex = normalized.indexOf("@");

		if (atIndex > 64) {
		    throw new IllegalArgumentException("Email local part exceeds maximum length.");
		}
	
		value = normalized;
	}

	// Factory method
	public static EmailAddress of(String value) {
		return new EmailAddress(value);
	}

	@Override
	public String toString() {
		return value;
	}

}
