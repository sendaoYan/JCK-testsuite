/*
 * @test
 * @summary java.net.http.WebSocketHandshakeException conformance: white-box structure + black-box response/cause.
 * @run main WebSocketHandshakeExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.http.HttpResponse;
import java.net.http.WebSocketHandshakeException;

public class WebSocketHandshakeExceptionTest {
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
        Class<?> c = Class.forName("java.net.http.WebSocketHandshakeException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == IOException.class, "superclass is IOException");
        check(c.getConstructor(HttpResponse.class) != null, "HttpResponse constructor present");
    }

    static void testBlackBox() {
        HttpResponse<?> response = (HttpResponse<?>) Proxy.newProxyInstance(
                WebSocketHandshakeExceptionTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.class},
                (p, m, a) -> m.getName().equals("statusCode") ? 403 : null);

        WebSocketHandshakeException e = new WebSocketHandshakeException(response);
        check(e.getResponse() == response, "getResponse() returns constructor argument");
        check(e instanceof IOException, "is an IOException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");

        Exception cause = new Exception("cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() is the supplied cause");
        expect(IllegalStateException.class, () -> e.initCause(new Exception("second")), "double initCause throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
