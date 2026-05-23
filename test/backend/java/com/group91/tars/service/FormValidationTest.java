package com.group91.tars.service;

import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests for form validation logic.
 * Validates: email format, deadline format, student number uniqueness.
 */
public class FormValidationTest {

    private TarsService service;

    @Before
    public void setUp() {
        service = TarsService.getInstance();
    }

    // ===================== Email Validation =====================

    @Test
    public void isValidBuptEmail_acceptsValidBuptEmail() {
        assertTrue(service.isValidBuptEmailPublic("student@bupt.edu.cn"));
        assertTrue(service.isValidBuptEmailPublic("zhangsan@bupt.edu.cn"));
        assertTrue(service.isValidBuptEmailPublic("test123@bupt.edu.cn"));
    }

    @Test
    public void isValidBuptEmail_rejectsNonBuptEmails() {
        assertFalse(service.isValidBuptEmailPublic("student@gmail.com"));
        assertFalse(service.isValidBuptEmailPublic("student@163.com"));
        assertFalse(service.isValidBuptEmailPublic("student.edu.cn"));
    }

    @Test
    public void isValidBuptEmail_rejectsEmptyAndNull() {
        assertFalse(service.isValidBuptEmailPublic(""));
        assertFalse(service.isValidBuptEmailPublic(null));
    }

    @Test
    public void isValidBuptEmail_rejectsEmailWithoutAtSymbol() {
        assertFalse(service.isValidBuptEmailPublic("studentbupt.edu.cn"));
        assertFalse(service.isValidBuptEmailPublic("student@"));
    }

    // ===================== Deadline Validation =====================

    @Test
    public void isValidFutureDeadline_acceptsFutureDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String futureDate = sdf.format(new Date(System.currentTimeMillis() + 86400000L * 30));
        assertTrue(service.isValidFutureDeadlinePublic(futureDate));
    }

    @Test
    public void isValidFutureDeadline_rejectsPastDate() {
        assertFalse(service.isValidFutureDeadlinePublic("2020-01-01"));
    }

    @Test
    public void isValidFutureDeadline_rejectsToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        assertFalse(service.isValidFutureDeadlinePublic(today));
    }

    @Test
    public void isValidFutureDeadline_rejectsInvalidFormat() {
        assertFalse(service.isValidFutureDeadlinePublic("2026/01/01"));
        assertFalse(service.isValidFutureDeadlinePublic("invalid"));
        assertFalse(service.isValidFutureDeadlinePublic(""));
        assertFalse(service.isValidFutureDeadlinePublic(null));
    }

    // ===================== Student Number Validation =====================
    // Test data: ta-1=231224653, ta-2=231224859, ta-3=231224612

    @Test
    public void isDuplicateStudentNumber_returnsTrue_whenOtherTaHasSameNumber() {
        // ta-1 uses 231224653, so ta-2 trying to use it should be duplicate
        boolean result = service.isDuplicateStudentNumberPublic("ta-2", "231224653");
        assertTrue("Student number used by ta-1 should be duplicate for ta-2", result);
    }

    @Test
    public void isDuplicateStudentNumber_returnsFalse_forUnusedNumber() {
        // A truly new student number should not be duplicate
        boolean result = service.isDuplicateStudentNumberPublic("ta-1", "888888888");
        assertFalse("New student number should not be duplicate", result);
    }

    @Test
    public void isDuplicateStudentNumber_handlesEmptyInput() {
        assertFalse(service.isDuplicateStudentNumberPublic("ta-1", ""));
        assertFalse(service.isDuplicateStudentNumberPublic("ta-1", null));
    }

    // ===================== Profile Validation =====================

    @Test
    public void saveTaProfile_rejectsInvalidEmail() {
        TAProfile profile = createValidProfile();
        profile.setEmail("invalid-email");

        OperationResult result = service.saveTaProfile("ta-profile-email-test", profile);
        assertFalse("Should reject invalid email format", result.isSuccess());
    }

    @Test
    public void saveTaProfile_acceptsValidProfile() {
        TAProfile profile = createValidProfile();
        String uniqueId = "ta-profile-valid-" + System.currentTimeMillis();
        profile.setStudentNumber(uniqueId);

        OperationResult result = service.saveTaProfile(uniqueId, profile);
        assertTrue("Valid profile should be saved: " + result.getMessage(), result.isSuccess());
    }

    @Test
    public void saveTaProfile_rejectsDuplicateStudentNumber() {
        // Try to use ta-2's student number for a new profile
        TAProfile profile = createValidProfile();
        profile.setStudentNumber("231224859"); // ta-2's student number

        OperationResult result = service.saveTaProfile("ta-profile-dup-test", profile);
        assertFalse("Should reject duplicate student number", result.isSuccess());
    }

    // ===================== Helper =====================

    private TAProfile createValidProfile() {
        TAProfile profile = new TAProfile();
        profile.setFullName("Test User");
        profile.setStudentNumber("PROFILE" + System.currentTimeMillis());
        profile.setEmail("test@bupt.edu.cn");
        profile.setPhone("12345678901");
        profile.setSkills("Java, Python");
        profile.setAvailability("Weekdays");
        return profile;
    }
}
