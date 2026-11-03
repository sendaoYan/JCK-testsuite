/*
 * @test
 * @summary java.security.spec.InvalidKeySpecException conformance: white-box structure + black-box behavior.
 * @run main InvalidKeySpecExceptionTest
 */

import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.security.spec.InvalidKeySpecException;

public class InvalidKeySpecExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = InvalidKeySpecException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass GeneralSecurityException");
    }

    static void testBlackBox() {
        InvalidKeySpecException e0 = new InvalidKeySpecException();
        check(e0.getMessage() == null, "no-arg getMessage null");

        InvalidKeySpecException e1 = new InvalidKeySpecException("boom");
        check("boom".equals(e1.getMessage()), "String getMessage");
        check(e1.getCause() == null, "String getCause null");

        Throwable cause = new IllegalStateException("c");
        InvalidKeySpecException e2 = new InvalidKeySpecException("boom", cause);
        check("boom".equals(e2.getMessage()), "String+Throwable getMessage");
        check(cause == e2.getCause(), "String+Throwable getCause");

        InvalidKeySpecException e3 = new InvalidKeySpecException(cause);
        check(cause == e3.getCause(), "Throwable getCause");
        check(e3.getMessage() != null, "Throwable getMessage non-null");

        check(e0 instanceof GeneralSecurityException, "instanceof GeneralSecurityException");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
