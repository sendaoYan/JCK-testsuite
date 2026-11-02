/*
 * @test
 * @summary java.net.http.StreamLimitException conformance: final IOException with version accessor.
 * @run main StreamLimitExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.http.HttpClient;
import java.net.http.StreamLimitException;

public class StreamLimitExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Modifier.isFinal(StreamLimitException.class.getModifiers()), "StreamLimitException is final");

        StreamLimitException e = new StreamLimitException(HttpClient.Version.HTTP_2, "stream limit reached");
        check("stream limit reached".equals(e.getMessage()), "message preserved");
        check(e.version() == HttpClient.Version.HTTP_2, "version() accessor");
        check(e instanceof IOException, "is an IOException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
