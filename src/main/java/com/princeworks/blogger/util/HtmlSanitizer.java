package com.princeworks.blogger.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {

    /**
     * Sanitizes HTML content to prevent XSS attacks while preserving rich text formatting
     * Allows: p, br, strong, b, em, i, u, h1-h6, ul, ol, li, blockquote
     */
    public String sanitize(String html) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }

        // Define allowed tags and attributes for rich text editing
        Safelist safelist = Safelist.basic()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6")
                .addTags("p", "br", "strong", "b", "em", "i", "u")
                .addTags("ul", "ol", "li")
                .addTags("blockquote")
                .addAttributes("blockquote", "style") // Allow inline styles for border-left
                .addAttributes("p", "style") // Allow text alignment
                .addAttributes("div", "style") // Allow text alignment
                .addAttributes("span", "style"); // Allow basic styling

        // Sanitize the HTML content
        String sanitized = Jsoup.clean(html, safelist);

        // Additional cleaning: remove any remaining script or event handlers
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "")
                            .replaceAll("(?i)javascript:", "")
                            .replaceAll("(?i)on\\w+\\s*=", "");

        return sanitized.trim();
    }

    /**
     * Strips HTML tags and returns plain text content (for validation purposes)
     */
    public String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }

        // Use Jsoup to parse and get text content
        String textContent = Jsoup.parse(html).text();

        // Clean up extra whitespace
        return textContent.replaceAll("\\s+", " ").trim();
    }
}
