/*
 * @test
 * @summary javax.naming.ldap.Rdn conformance: API type verification.
 * @run main RdnTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class RdnTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.Rdn r = new javax.naming.ldap.Rdn("cn=John");
check(r.getType().equals("cn"), "type");
check("John".equals(r.getValue()), "value");
check(r.toString().equals("cn=John"), "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
