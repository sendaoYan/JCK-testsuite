/*
 * @test
 * @summary javax.security.cert.CertificateException conformance: white-box structure + black-box behavior.
 * @run main CertificateExceptionTest
 */

import java.lang.reflect.*;
import javax.security.cert.CertificateException;

public class CertificateExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.cert.CertificateException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == java.lang.Exception.class, "extends java.lang.Exception");
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 0) noArg = true;
            if (p.length == 1 && p[0] == String.class) strArg = true;
        }
        check(noArg, "no-arg constructor");
        check(strArg, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CertificateException e = new CertificateException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new CertificateException().getMessage() == null, "no-arg null message");

        Throwable cause = new Throwable("cause");
        CertificateException x = new CertificateException();
        check(x.initCause(cause) == x, "initCause returns this");
        check(x.getCause() == cause, "getCause");
        check(x.toString().contains("CertificateException"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
