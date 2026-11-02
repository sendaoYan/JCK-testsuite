/*
 * @test
 * @summary java.net.http.HttpConnectTimeoutException conformance: message and exception hierarchy.
 * @run main HttpConnectTimeoutExceptionTest
 */

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;

public class HttpConnectTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        HttpConnectTimeoutException e = new HttpConnectTimeoutException("connect timed out");
        check("connect timed out".equals(e.getMessage()), "message preserved");
        check(e instanceof HttpTimeoutException, "is a HttpTimeoutException");
        check(e instanceof IOException, "is an IOException");
        check(e.getClass().getSuperclass() == HttpTimeoutException.class,
                "superclass is HttpTimeoutException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
