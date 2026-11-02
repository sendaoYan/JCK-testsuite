/*
 * @test
 * @summary java.net.http.HttpTimeoutException conformance: message and exception hierarchy.
 * @run main HttpTimeoutExceptionTest
 */

import java.io.IOException;
import java.net.http.HttpTimeoutException;

public class HttpTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        HttpTimeoutException e = new HttpTimeoutException("request timed out");
        check("request timed out".equals(e.getMessage()), "message preserved");
        check(e instanceof IOException, "is an IOException");
        check(e.getClass().getSuperclass() == IOException.class, "superclass is IOException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
