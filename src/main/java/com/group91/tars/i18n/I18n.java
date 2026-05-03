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

public class I18n {
    private static final String BASE_NAME = "i18n.messages";

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

    public I18n(Locale locale) {
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle(BASE_NAME, locale, UTF8_CONTROL);
    }

    /** Retrieve I18n stored as request attribute by BasePageServlet. */
    public static I18n fromRequest(HttpServletRequest request) {
        return (I18n) request.getAttribute("i18n");
    }

    /** Translate a key. Returns "!KEY!" as visible fallback on miss. */
    public String t(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    /** Translate key with {0}, {1} positional parameters. */
    public String t(String key, Object... args) {
        String pattern = t(key);
        return MessageFormat.format(pattern, args);
    }

    /** Two-letter language code (e.g. "en", "zh"). */
    public String getLanguage() {
        return locale.getLanguage();
    }
}
