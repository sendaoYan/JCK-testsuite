/*
 * @test
 * @summary javax.naming.ldap.SortControl conformance: API type verification.
 * @run main SortControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SortControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.SortControl c = new javax.naming.ldap.SortControl("cn", true);
check(c.getID().equals(javax.naming.ldap.SortControl.OID), "oid");
check(c.isCritical(), "critical");
check(c instanceof javax.naming.ldap.BasicControl, "BasicControl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
