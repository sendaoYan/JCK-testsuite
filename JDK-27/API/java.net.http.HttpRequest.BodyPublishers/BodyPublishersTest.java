/*
 * @test
 * @summary java.net.http.HttpRequest.BodyPublishers conformance: static factory content lengths.
 * @run main BodyPublishersTest
 */

import java.io.ByteArrayInputStream;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class BodyPublishersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpRequest.BodyPublishers.ofString("abc").contentLength() == 3, "ofString length");
        check(HttpRequest.BodyPublishers.ofString("abc", StandardCharsets.UTF_8).contentLength() == 3,
                "ofString with charset length");
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}).contentLength() == 3,
                "ofByteArray length");
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3, 4}, 1, 2).contentLength() == 2,
                "ofByteArray with offset length");
        check(HttpRequest.BodyPublishers.noBody().contentLength() == 0, "noBody length 0");
        check(HttpRequest.BodyPublishers.ofInputStream(
                () -> new ByteArrayInputStream(new byte[]{1, 2})).contentLength() == -1,
                "ofInputStream unknown length -1");
        check(HttpRequest.BodyPublishers.concat(
                HttpRequest.BodyPublishers.noBody(),
                HttpRequest.BodyPublishers.ofString("x")).contentLength() == 1,
                "concat length");
        check(HttpRequest.BodyPublishers.fromPublisher(
                HttpRequest.BodyPublishers.noBody()).contentLength() == -1,
                "fromPublisher unknown length -1");

        HttpRequest.BodyPublisher p = HttpRequest.BodyPublishers.ofString("hello");
        check(p instanceof HttpRequest.BodyPublisher, "ofString returns a BodyPublisher");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
