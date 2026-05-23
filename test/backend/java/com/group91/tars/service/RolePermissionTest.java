package com.group91.tars.service;

import com.group91.tars.model.UserAccount;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for role-based permission isolation.
 * Verifies that MO users are restricted to EBU6304 data only,
 * TA users can access all courses, and Admin has full access.
 */
public class RolePermissionTest {

    private TarsService service;

    @Before
    public void setUp() {
        service = TarsService.getInstance();
    }

    // ===================== Role Type Checks =====================

    @Test
    public void isMo_returnsTrue_forMoRole() {
        assertTrue(service.isMo("MO"));
    }

    @Test
    public void isMo_returnsFalse_forOtherRoles() {
        assertFalse(service.isMo("TA"));
        assertFalse(service.isMo("ADMIN"));
        assertFalse(service.isMo(""));
        assertFalse(service.isMo(null));
    }

    @Test
    public void isAdmin_returnsTrue_forAdminRole() {
        assertTrue(service.isAdmin("ADMIN"));
    }

    @Test
    public void isAdmin_returnsFalse_forOtherRoles() {
        assertFalse(service.isAdmin("MO"));
        assertFalse(service.isAdmin("TA"));
        assertFalse(service.isAdmin(""));
        assertFalse(service.isAdmin(null));
    }

    @Test
    public void isTa_returnsTrue_forTaRole() {
        assertTrue(service.isTa("TA"));
    }

    @Test
    public void isTa_returnsFalse_forOtherRoles() {
        assertFalse(service.isTa("MO"));
        assertFalse(service.isTa("ADMIN"));
        assertFalse(service.isTa(""));
        assertFalse(service.isTa(null));
    }

    // ===================== MO Restriction Check =====================

    @Test
    public void isMoRestricted_returnsTrue_forMoUser() {
        UserAccount moUser = new UserAccount();
        moUser.setRole("MO");
        assertTrue(service.isMoRestricted(moUser));
    }

    @Test
    public void isMoRestricted_returnsFalse_forTaUser() {
        UserAccount taUser = new UserAccount();
        taUser.setRole("TA");
        assertFalse(service.isMoRestricted(taUser));
    }

    @Test
    public void isMoRestricted_returnsFalse_forAdminUser() {
        UserAccount adminUser = new UserAccount();
        adminUser.setRole("ADMIN");
        assertFalse(service.isMoRestricted(adminUser));
    }

    @Test
    public void isMoRestricted_returnsFalse_forNullUser() {
        assertFalse(service.isMoRestricted(null));
    }

    // ===================== Unrestricted Read Check =====================

    @Test
    public void isUnrestrictedRead_returnsTrue_forTaUser() {
        UserAccount taUser = new UserAccount();
        taUser.setRole("TA");
        assertTrue(service.isUnrestrictedRead(taUser));
    }

    @Test
    public void isUnrestrictedRead_returnsTrue_forAdminUser() {
        UserAccount adminUser = new UserAccount();
        adminUser.setRole("ADMIN");
        assertTrue(service.isUnrestrictedRead(adminUser));
    }

    @Test
    public void isUnrestrictedRead_returnsFalse_forMoUser() {
        UserAccount moUser = new UserAccount();
        moUser.setRole("MO");
        assertFalse(service.isUnrestrictedRead(moUser));
    }

    @Test
    public void isUnrestrictedRead_returnsFalse_forNullUser() {
        assertFalse(service.isUnrestrictedRead(null));
    }

    // ===================== MO Owned Job Check =====================

    @Test
    public void isMoOwnedJob_returnsTrue_forEbU6304Job() {
        com.group91.tars.model.JobPosting job = new com.group91.tars.model.JobPosting();
        job.setModuleCode("EBU6304");
        assertTrue(service.isMoOwnedJob(job));
    }

    @Test
    public void isMoOwnedJob_returnsFalse_forOtherCourseJob() {
        com.group91.tars.model.JobPosting job = new com.group91.tars.model.JobPosting();
        job.setModuleCode("EIE3320");
        assertFalse(service.isMoOwnedJob(job));
    }

    @Test
    public void isMoOwnedJob_returnsFalse_forNullJob() {
        assertFalse(service.isMoOwnedJob(null));
    }

    // ===================== Course Code Validation =====================

    @Test
    public void isValidMoCourseCode_returnsTrue_forEbU6304() {
        assertTrue(service.isValidMoCourseCode("EBU6304"));
    }

    @Test
    public void isValidMoCourseCode_returnsFalse_forOtherCodes() {
        assertFalse(service.isValidMoCourseCode("EIE3320"));
        assertFalse(service.isValidMoCourseCode("EBU5408"));
        assertFalse(service.isValidMoCourseCode(""));
        assertFalse(service.isValidMoCourseCode(null));
    }
}
