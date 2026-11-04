/*
 * @test
 * @summary javax.security.sasl.SaslException conformance: white-box structure + black-box behavior.
 * @run main SaslExceptionTest
 */

import java.lang.reflect.*;
import javax.security.sasl.SaslException;

public class SaslExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.SaslException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == java.io.IOException.class, "extends IOException");
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
        SaslException e = new SaslException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.io.IOException, "is IOException");
        check(new SaslException().getMessage() == null, "no-arg null message");

        Throwable cause = new Throwable("cause");
        SaslException x = new SaslException("m", cause);
        check("m".equals(x.getMessage()), "message with cause");
        check(x.getCause() == cause, "cause ctor");

        Throwable cause2 = new Throwable("c2");
        SaslException y = new SaslException();
        check(y.initCause(cause2) == y, "initCause returns this");
        check(y.getCause() == cause2, "getCause");
        check(y.toString().contains("SaslException"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
