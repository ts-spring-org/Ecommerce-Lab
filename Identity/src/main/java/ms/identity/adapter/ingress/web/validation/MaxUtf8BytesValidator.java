package ms.identity.adapter.ingress.web.validation;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxUtf8BytesValidator implements ConstraintValidator<MaxUtf8Bytes, String> {
	private int max;

	@Override
	public void initialize(MaxUtf8Bytes constraintAnnotation) {
		this.max = constraintAnnotation.max();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true; // Consider null as valid, use @NotNull for null checks
		}
		return value.getBytes(StandardCharsets.UTF_8).length <= max;
	}

}
