package ms.identity.adapter.ingress.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxUtf8Bytes {
	String message() default "must not exceed {max} UTF-8 bytes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int max();
}
