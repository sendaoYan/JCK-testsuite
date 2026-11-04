/*
 * @test
 * @summary javax.security.sasl.AuthenticationException conformance: white-box structure + black-box behavior.
 * @run main AuthenticationExceptionTest
 */

import java.lang.reflect.*;
import javax.security.sasl.AuthenticationException;
import javax.security.sasl.SaslException;

public class AuthenticationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.AuthenticationException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == SaslException.class, "extends SaslException");
        boolean noArg = false, strArg = false, strThrowable = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 0) noArg = true;
            if (p.length == 1 && p[0] == String.class) strArg = true;
            if (p.length == 2 && p[0] == String.class && p[1] == Throwable.class) strThrowable = true;
        }
        check(noArg, "no-arg constructor");
        check(strArg, "String constructor");
        check(strThrowable, "String,Throwable constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AuthenticationException e = new AuthenticationException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof SaslException, "is SaslException");
        check(e instanceof java.io.IOException, "is IOException");
        check(new AuthenticationException().getMessage() == null, "no-arg null message");

        Throwable cause = new Throwable("cause");
        AuthenticationException x = new AuthenticationException("m", cause);
        check("m".equals(x.getMessage()), "message with cause");
        check(x.getCause() == cause, "cause ctor");

        Throwable cause2 = new Throwable("c2");
        AuthenticationException y = new AuthenticationException();
        check(y.initCause(cause2) == y, "initCause returns this");
        check(y.getCause() == cause2, "getCause");
        check(y.toString().contains("AuthenticationException"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
