/*
 * @test
 * @summary java.security.cert.CRLException conformance: white-box structure + black-box behavior.
 * @run main CRLExceptionTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class CRLExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.CRLException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(new CRLException().getMessage() == null, "no-arg message null");
        check(new CRLException("m").getMessage().equals("m"), "message exact");
        Throwable cause = new java.io.IOException("io");
        check(new CRLException("m", cause).getCause() == cause, "cause retained");
        check(new CRLException(cause).getMessage().equals("java.io.IOException: io"), "cause message mirrors");
        check(new CRLException() instanceof java.security.GeneralSecurityException, "GeneralSecurityException family");
        // real raise path: decoding garbage bytes
        try {
            CertificateFactory.getInstance("X.509").generateCRL(new ByteArrayInputStream(new byte[]{7, 7, 7}));
            check(false, "garbage CRL throws");
        } catch (CRLException ce) {
            check(true, "garbage CRL CRLException");
        }

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
