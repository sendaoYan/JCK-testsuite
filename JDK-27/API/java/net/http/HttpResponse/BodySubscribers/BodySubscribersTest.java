/*
 * @test
 * @summary java.net.http.HttpResponse.BodySubscribers conformance: static factories return non-null subscribers.
 * @run main BodySubscribersTest
 */

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class BodySubscribersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8) != null, "ofString");
        check(HttpResponse.BodySubscribers.ofByteArray() != null, "ofByteArray");
        check(HttpResponse.BodySubscribers.ofFile(Path.of("/tmp/jck-bodysubscriber")) != null, "ofFile");
        check(HttpResponse.BodySubscribers.ofInputStream() != null, "ofInputStream");
        check(HttpResponse.BodySubscribers.ofLines(StandardCharsets.UTF_8) != null, "ofLines");
        check(HttpResponse.BodySubscribers.ofPublisher() != null, "ofPublisher");
        check(HttpResponse.BodySubscribers.replacing("u") != null, "replacing");
        check(HttpResponse.BodySubscribers.discarding() != null, "discarding");
        check(HttpResponse.BodySubscribers.buffering(HttpResponse.BodySubscribers.ofByteArray(), 16) != null,
                "buffering");
        check(HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofByteArray(),
                bytes -> new String(bytes, StandardCharsets.UTF_8)) != null, "mapping");
        check(HttpResponse.BodySubscribers.limiting(HttpResponse.BodySubscribers.ofByteArray(), 100) != null,
                "limiting");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
