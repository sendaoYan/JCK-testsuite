/*
 * @test
 * @summary javax.naming.ldap.PagedResultsControl conformance: API type verification.
 * @run main PagedResultsControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class PagedResultsControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.PagedResultsControl c = new javax.naming.ldap.PagedResultsControl(10, true);
check(c.getID().equals(javax.naming.ldap.PagedResultsControl.OID), "oid");
check(c.isCritical(), "critical");
check(c.getEncodedValue() != null, "encoded");
check(c instanceof javax.naming.ldap.BasicControl, "BasicControl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
