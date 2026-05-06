package com.group91.tars.i18n;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Internationalisation utility providing locale-aware message translation.
 * Loads messages from UTF-8 encoded {@code .properties} files and supports
 * positional parameter substitution via {@link MessageFormat}.
 */
public class I18n {
    private static final String BASE_NAME = "i18n.messages";

    /** Custom ResourceBundle.Control that reads properties files as UTF-8. */
    private static final ResourceBundle.Control UTF8_CONTROL = new ResourceBundle.Control() {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                         ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            if ("java.properties".equals(format)) {
                String resourceName = toResourceName(toBundleName(baseName, locale), "properties");
                InputStream stream = loader.getResourceAsStream(resourceName);
                if (stream != null) {
                    try (Reader reader = new InputStreamReader(stream, "UTF-8")) {
                        return new PropertyResourceBundle(reader);
                    }
                }
            }
            return super.newBundle(baseName, locale, format, loader, reload);
        }
    };

    private final ResourceBundle bundle;
    private final Locale locale;

    /**
     * Constructs an I18n instance for the given locale.
     *
     * @param locale the target locale for message translation
     */
    public I18n(Locale locale) {
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle(BASE_NAME, locale, UTF8_CONTROL);
    }

    /**
     * Retrieves the I18n instance stored as a request attribute by BasePageServlet.
     *
     * @param request the HTTP request containing the "i18n" attribute
     * @return the I18n instance
     */
    public static I18n fromRequest(HttpServletRequest request) {
        return (I18n) request.getAttribute("i18n");
    }

    /**
     * Translates a message key to the current locale's text.
     * Returns {@code "!KEY!"} as a visible fallback when the key is not found.
     *
     * @param key the i18n property key
     * @return the translated string, or "!key!" if not found
     */
    public String t(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    /**
     * Translates a message key with positional parameters using {@link MessageFormat}.
     *
     * @param key  the i18n property key
     * @param args positional arguments to substitute into the pattern
     * @return the formatted translated string
     */
    public String t(String key, Object... args) {
        String pattern = t(key);
        return MessageFormat.format(pattern, args);
    }

    /**
     * Returns the two-letter language code of the current locale.
     *
     * @return language code (e.g. "en", "zh")
     */
    public String getLanguage() {
        return locale.getLanguage();
    }
}
