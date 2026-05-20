package com.group91.tars.service.ai;

import com.group91.tars.model.TAProfile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class CvTextExtractor {
    private static final int MAX_CHARS = 4000;

    public String extract(TAProfile profile) {
        if (profile == null || isBlank(profile.getCvPath())) {
            return "CV not available.";
        }
        String cvPath = profile.getCvPath();
        if (!cvPath.toLowerCase(Locale.ENGLISH).endsWith(".pdf")) {
            return "CV analysis supports PDF files only.";
        }

        File file = resolveFile(cvPath);
        if (!file.isFile()) {
            return "CV not available.";
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            return truncate(normalize(stripper.getText(document)));
        } catch (IOException exception) {
            return "CV could not be parsed.";
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException ignored) {
                    // Nothing useful for the JSP layer to do here.
                }
            }
        }
    }

    private File resolveFile(String cvPath) {
        File file = new File(cvPath);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(System.getProperty("user.dir"), cvPath);
    }

    private String normalize(String text) {
        if (isBlank(text)) {
            return "CV could not be parsed.";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String text) {
        if (text == null || text.length() <= MAX_CHARS) {
            return text;
        }
        return text.substring(0, MAX_CHARS);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
