/*
 * @test
 * @summary java.net.http.HttpResponse.BodyHandlers conformance: static factories return non-null handlers.
 * @run main BodyHandlersTest
 */

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Flow;

public class BodyHandlersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final Flow.Subscriber<? super List<ByteBuffer>> BYTE_SUB = new Flow.Subscriber<>() {
        public void onSubscribe(Flow.Subscription s) { s.cancel(); }
        public void onNext(List<ByteBuffer> item) { }
        public void onError(Throwable t) { }
        public void onComplete() { }
    };

    static final Flow.Subscriber<? super String> LINE_SUB = new Flow.Subscriber<>() {
        public void onSubscribe(Flow.Subscription s) { s.cancel(); }
        public void onNext(String item) { }
        public void onError(Throwable t) { }
        public void onComplete() { }
    };

    public static void main(String[] args) {
        check(HttpResponse.BodyHandlers.discarding() != null, "discarding");
        check(HttpResponse.BodyHandlers.ofString() != null, "ofString");
        check(HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8) != null, "ofString charset");
        check(HttpResponse.BodyHandlers.ofByteArray() != null, "ofByteArray");
        check(HttpResponse.BodyHandlers.ofLines() != null, "ofLines");
        check(HttpResponse.BodyHandlers.ofInputStream() != null, "ofInputStream");
        check(HttpResponse.BodyHandlers.ofPublisher() != null, "ofPublisher");
        check(HttpResponse.BodyHandlers.replacing("default") != null, "replacing");
        check(HttpResponse.BodyHandlers.ofByteArrayConsumer(opt -> { }) != null, "ofByteArrayConsumer");
        check(HttpResponse.BodyHandlers.ofFile(Path.of("/tmp/jck-bodyhandler")) != null, "ofFile");
        check(HttpResponse.BodyHandlers.ofFileDownload(
                Path.of(System.getProperty("java.io.tmpdir"))) != null, "ofFileDownload");
        check(HttpResponse.BodyHandlers.buffering(HttpResponse.BodyHandlers.ofString(), 10) != null,
                "buffering");
        check(HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofString(), 100) != null,
                "limiting");
        check(HttpResponse.BodyHandlers.fromSubscriber(BYTE_SUB) != null, "fromSubscriber");
        check(HttpResponse.BodyHandlers.fromSubscriber(BYTE_SUB, s -> "x") != null, "fromSubscriber func");
        check(HttpResponse.BodyHandlers.fromLineSubscriber(LINE_SUB) != null, "fromLineSubscriber");
        check(HttpResponse.BodyHandlers.fromLineSubscriber(LINE_SUB, s -> "x", "\n") != null,
                "fromLineSubscriber func");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
