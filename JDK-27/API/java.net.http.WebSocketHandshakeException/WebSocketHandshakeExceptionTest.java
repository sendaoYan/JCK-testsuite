/*
 * @test
 * @summary java.net.http.WebSocketHandshakeException conformance: final IOException carrying a response.
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

    public static void main(String[] args) {
        check(Modifier.isFinal(WebSocketHandshakeException.class.getModifiers()),
                "WebSocketHandshakeException is final");

        HttpResponse<?> response = (HttpResponse<?>) Proxy.newProxyInstance(
                WebSocketHandshakeExceptionTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.class},
                (p, m, a) -> m.getName().equals("statusCode") ? 403 : null);

        WebSocketHandshakeException e = new WebSocketHandshakeException(response);
        check(e.getResponse() == response, "getResponse() returns constructor argument");
        check(e instanceof IOException, "is an IOException");

        Exception cause = new Exception("cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() is the supplied cause");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
