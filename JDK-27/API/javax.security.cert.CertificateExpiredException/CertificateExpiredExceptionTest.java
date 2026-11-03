/*
 * @test
 * @summary javax.security.cert.CertificateExpiredException conformance: white-box structure + black-box behavior.
 * @run main CertificateExpiredExceptionTest
 */

import java.lang.reflect.*;
import javax.security.cert.CertificateExpiredException;

public class CertificateExpiredExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.cert.CertificateExpiredException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.security.cert.CertificateException"), "extends CertificateException");
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
        CertificateExpiredException e = new CertificateExpiredException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.security.cert.CertificateException, "is CertificateException");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new CertificateExpiredException().getMessage() == null, "no-arg null message");

        Throwable cause = new Throwable("cause");
        CertificateExpiredException x = new CertificateExpiredException();
        check(x.initCause(cause) == x, "initCause returns this");
        check(x.getCause() == cause, "getCause");
        check(x.toString().contains("CertificateExpiredException"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
