/*
 * @test
 * @summary java.net.http.HttpConnectTimeoutException conformance: white-box structure + black-box exception chain.
 * @run main HttpConnectTimeoutExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;

public class HttpConnectTimeoutExceptionTest {
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
        Class<?> c = Class.forName("java.net.http.HttpConnectTimeoutException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == HttpTimeoutException.class, "superclass is HttpTimeoutException");
        check(c.getConstructor(String.class) != null, "String constructor present");
    }

    static void testBlackBox() {
        HttpConnectTimeoutException e = new HttpConnectTimeoutException("connect timed out");
        check("connect timed out".equals(e.getMessage()), "message preserved");
        check(e instanceof HttpTimeoutException, "is a HttpTimeoutException");
        check(e instanceof IOException, "is an IOException");
        check(e.getClass().getSuperclass() == HttpTimeoutException.class, "superclass is HttpTimeoutException");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");
        check(e.toString().contains("connect timed out"), "toString contains message");

        Exception cause = new IllegalStateException("root");
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
