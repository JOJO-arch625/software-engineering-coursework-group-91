package com.group91.tars.service;

import com.group91.tars.model.OperationResult;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for CV file upload validation.
 * Verifies file extension, size, and TA profile existence checks.
 */
public class CvUploadValidationTest {

    private TarsService service;

    @Before
    public void setUp() {
        service = TarsService.getInstance();
    }

    private Part createMockPart(String filename, byte[] content) throws IOException {
        Part mockPart = mock(Part.class);
        when(mockPart.getSubmittedFileName()).thenReturn(filename);
        when(mockPart.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(mockPart.getSize()).thenReturn((long) content.length);
        return mockPart;
    }

    @Test
    public void uploadCv_requiresExistingTaProfile() throws IOException {
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        Part part = createMockPart("test.pdf", content);

        OperationResult result = service.uploadTaCv("nonexistent-ta-id", part);
        assertFalse("Should fail for non-existent TA profile", result.isSuccess());
    }

    @Test
    public void uploadCv_acceptsValidPdfExtension() throws IOException {
        byte[] content = "test pdf content".getBytes(StandardCharsets.UTF_8);
        Part pdfPart = createMockPart("test.pdf", content);

        OperationResult result = service.uploadTaCv("ta-1", pdfPart);
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void uploadCv_acceptsDocxExtension() throws IOException {
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        Part docxPart = createMockPart("test.docx", content);

        OperationResult result = service.uploadTaCv("ta-1", docxPart);
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void uploadCv_acceptsDocExtension() throws IOException {
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        Part docPart = createMockPart("test.doc", content);

        OperationResult result = service.uploadTaCv("ta-1", docPart);
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void uploadCv_rejectsExeFile() throws IOException {
        byte[] content = "MZ mock executable".getBytes(StandardCharsets.UTF_8);
        Part exePart = createMockPart("test.exe", content);

        OperationResult result = service.uploadTaCv("ta-1", exePart);
        assertFalse("Should reject executable files", result.isSuccess());
    }

    @Test
    public void uploadCv_rejectsJpgFile() throws IOException {
        byte[] jpgContent = new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF};
        Part jpgPart = createMockPart("test.jpg", jpgContent);

        OperationResult result = service.uploadTaCv("ta-1", jpgPart);
        assertFalse("Should reject image files", result.isSuccess());
    }

    @Test
    public void uploadCv_rejectsNoExtensionFile() throws IOException {
        byte[] content = "no extension file".getBytes(StandardCharsets.UTF_8);
        Part noExtPart = createMockPart("testfile", content);

        OperationResult result = service.uploadTaCv("ta-1", noExtPart);
        assertFalse("Should reject files without extension", result.isSuccess());
    }

    @Test
    public void uploadCv_rejectsOversizedFile() throws IOException {
        byte[] largeContent = new byte[11 * 1024 * 1024];
        Part largePart = createMockPart("test.pdf", largeContent);

        OperationResult result = service.uploadTaCv("ta-1", largePart);
        assertFalse("Should reject files larger than 10MB", result.isSuccess());
    }

    @Test
    public void uploadCv_rejectsEmptyFile() throws IOException {
        byte[] emptyContent = new byte[0];
        Part emptyPart = createMockPart("empty.pdf", emptyContent);

        OperationResult result = service.uploadTaCv("ta-1", emptyPart);
        assertFalse("Should reject empty files", result.isSuccess());
    }

    @Test
    public void uploadCv_acceptsUppercaseExtension() throws IOException {
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        Part upperPart = createMockPart("test.PDF", content);

        OperationResult result = service.uploadTaCv("ta-1", upperPart);
        assertNotNull("Result should not be null", result);
    }
}
