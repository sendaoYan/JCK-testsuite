/*
 * @test
 * @summary javax.naming.ReferralException conformance: API type verification.
 * @run main ReferralExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ReferralExceptionTest {
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
        Class<?> c = Class.forName("javax.naming.ReferralException");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(c.getSuperclass().getName().equals("javax.naming.NamingException"), "extends javax.naming.NamingException");
check(hasMethod(c, "getReferralInfo"), "getReferralInfo present");
check(hasMethod(c, "getReferralContext"), "getReferralContext present");
check(hasMethod(c, "skipReferral"), "skipReferral present");
check(hasMethod(c, "retryReferral"), "retryReferral present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
