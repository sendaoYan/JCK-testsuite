/*
 * @test
 * @summary java.nio.charset.CoderMalfunctionError conformance: white-box structure + black-box cause chain.
 * @run main CoderMalfunctionErrorTest
 */

import java.lang.reflect.Modifier;
import java.nio.charset.CoderMalfunctionError;

public class CoderMalfunctionErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.CoderMalfunctionError");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(c.getConstructor(Exception.class) != null, "Exception constructor present");
    }

    static void testBlackBox() {
        Exception cause = new Exception("cause");
        CoderMalfunctionError e = new CoderMalfunctionError(cause);
        check(e instanceof Error, "is Error");
        check(e.getCause() == cause, "getCause");
        check(e.getMessage().equals(cause.toString()), "message is cause.toString()");
        check(e.getStackTrace().length > 0, "stack trace captured");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
