/*
 * @test
 * @summary java.security.cert.CertificateEncodingException conformance: white-box structure + black-box behavior.
 * @run main CertificateEncodingExceptionTest
 */

import java.lang.reflect.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;



public class CertificateEncodingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.CertificateEncodingException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.cert.CertificateException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(new CertificateEncodingException().getMessage() == null, "no-arg null message");
        CertificateEncodingException e = new CertificateEncodingException("encode failed");
        check(e.getMessage().equals("encode failed"), "message exact");
        Throwable cause = new IllegalStateException("boom");
        CertificateEncodingException e2 = new CertificateEncodingException("wrap", cause);
        check(e2.getCause() == cause && e2.getMessage().equals("wrap"), "message+cause");
        CertificateEncodingException e3 = new CertificateEncodingException(cause);
        check(e3.getCause() == cause, "cause-only ctor");
        check(e instanceof CertificateException, "extends CertificateException");

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
