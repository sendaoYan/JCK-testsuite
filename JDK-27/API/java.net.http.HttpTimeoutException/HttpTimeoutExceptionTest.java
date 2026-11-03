/*
 * @test
 * @summary java.net.http.HttpTimeoutException conformance: white-box structure + black-box exception chain.
 * @run main HttpTimeoutExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.http.HttpTimeoutException;

public class HttpTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpTimeoutException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IOException.class, "superclass is IOException");
        check(c.getConstructor(String.class) != null, "String constructor present");
    }

    static void testBlackBox() {
        HttpTimeoutException e = new HttpTimeoutException("request timed out");
        check("request timed out".equals(e.getMessage()), "message preserved");
        check(e instanceof IOException, "is an IOException");
        check(e.getClass().getSuperclass() == IOException.class, "superclass is IOException");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");

        Exception cause = new Exception("cause");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "getCause is the supplied cause");
        expect(IllegalStateException.class, () -> e.initCause(new Exception("second")), "double initCause throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
