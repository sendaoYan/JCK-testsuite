/*
 * @test
 * @summary java.security.spec.InvalidParameterSpecException conformance: white-box structure + black-box behavior.
 * @run main InvalidParameterSpecExceptionTest
 */

import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.security.spec.InvalidParameterSpecException;

public class InvalidParameterSpecExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = InvalidParameterSpecException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass GeneralSecurityException");
    }

    static void testBlackBox() {
        InvalidParameterSpecException e0 = new InvalidParameterSpecException();
        check(e0.getMessage() == null, "no-arg getMessage null");

        InvalidParameterSpecException e1 = new InvalidParameterSpecException("bad params");
        check("bad params".equals(e1.getMessage()), "String getMessage");
        check(e1.getCause() == null, "String getCause null");

        check(e0 instanceof GeneralSecurityException, "instanceof GeneralSecurityException");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
