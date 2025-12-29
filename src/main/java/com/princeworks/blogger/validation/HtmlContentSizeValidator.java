package com.princeworks.blogger.validation;

import com.princeworks.blogger.util.HtmlSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class HtmlContentSizeValidator implements ConstraintValidator<HtmlContentSize, String> {

    private int min;
    private int max;

    @Autowired
    private HtmlSanitizer htmlSanitizer;

    @Override
    public void initialize(HtmlContentSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return min == 0; // null is valid only if min is 0
        }

        // Strip HTML tags and get text content using the HtmlSanitizer
        String textContent = htmlSanitizer.stripHtmlTags(value);
        int length = textContent.length();

        return length >= min && length <= max;
    }
}
