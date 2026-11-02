/*
 * @test
 * @summary javax.naming.ldap.LdapReferralException conformance: API type verification.
 * @run main LdapReferralExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LdapReferralExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
static boolean hasMethod(Class<?> c, String n) {
    for (java.lang.reflect.Method m : c.getMethods()) {
        if (m.getName().equals(n)) return true;
    }
    return false;
}

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.LdapReferralException");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(c.getSuperclass().getName().equals("javax.naming.ReferralException"), "extends javax.naming.ReferralException");
check(hasMethod(c, "getReferralContext"), "getReferralContext present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
