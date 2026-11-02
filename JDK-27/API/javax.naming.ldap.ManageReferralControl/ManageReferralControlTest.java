/*
 * @test
 * @summary javax.naming.ldap.ManageReferralControl conformance: API type verification.
 * @run main ManageReferralControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ManageReferralControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.ManageReferralControl c = new javax.naming.ldap.ManageReferralControl();
check(c.getID().equals(javax.naming.ldap.ManageReferralControl.OID), "oid");
check(c.isCritical(), "critical");
check(c instanceof javax.naming.ldap.BasicControl, "BasicControl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
