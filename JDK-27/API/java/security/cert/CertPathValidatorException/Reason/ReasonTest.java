/*
 * @test
 * @summary java.security.cert.CertPathValidatorException.Reason conformance: white-box structure + black-box behavior.
 * @run main ReasonTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;


public class ReasonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.CertPathValidatorException$Reason");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Marker interface for validation failure causes; implemented by BasicReason and PKIXReason
        check(CertPathValidatorException.BasicReason.EXPIRED instanceof CertPathValidatorException.Reason, "BasicReason implements");
        check(PKIXReason.NO_TRUST_ANCHOR instanceof CertPathValidatorException.Reason, "PKIXReason implements");
        // custom implementation is accepted by the 5-arg constructor
        CertPathValidatorException.Reason custom = new CertPathValidatorException.Reason() {
            public String toString() { return "JCK_CUSTOM"; }
        };
        CertPathValidatorException e = new CertPathValidatorException("m", null, null, -1, custom);
        check(e.getReason() == custom, "custom reason retained");
        // default reason when not supplied
        CertPathValidatorException dflt = new CertPathValidatorException();
        check(dflt.getReason() == CertPathValidatorException.BasicReason.UNSPECIFIED, "default UNSPECIFIED");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
