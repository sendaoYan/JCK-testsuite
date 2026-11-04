/*
 * @test
 * @summary java.net.http.HttpResponse.BodyHandlers conformance: static factories return non-null handlers.
 * @run main BodyHandlersTest
 */

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class BodyHandlersTest {
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

    static final Flow.Subscription SUB = new Flow.Subscription() {
        public void request(long n) { }
        public void cancel() { }
    };
    static final HttpResponse.ResponseInfo INFO = new HttpResponse.ResponseInfo() {
        public int statusCode() { return 200; }
        public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
        public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    };

    static ByteBuffer bb(String s) { return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)); }

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

    public static void main(String[] args) throws Exception {
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

        // ---- deepened: every factory applies to a ResponseInfo producing a usable subscriber ----
        check(HttpResponse.BodyHandlers.discarding().apply(INFO) != null, "discarding().apply");
        check(HttpResponse.BodyHandlers.ofString().apply(INFO) != null, "ofString().apply");
        check(HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1).apply(INFO) != null,
                "ofString(cs).apply");
        check(HttpResponse.BodyHandlers.ofByteArray().apply(INFO) != null, "ofByteArray().apply");
        check(HttpResponse.BodyHandlers.ofLines().apply(INFO) != null, "ofLines().apply");
        check(HttpResponse.BodyHandlers.ofInputStream().apply(INFO) != null, "ofInputStream().apply");
        check(HttpResponse.BodyHandlers.ofPublisher().apply(INFO) != null, "ofPublisher().apply");
        check(HttpResponse.BodyHandlers.replacing("R").apply(INFO) != null, "replacing().apply");
        check(HttpResponse.BodyHandlers.buffering(
                HttpResponse.BodyHandlers.ofString(), 4).apply(INFO) != null, "buffering().apply");
        check(HttpResponse.BodyHandlers.limiting(
                HttpResponse.BodyHandlers.ofString(), 8).apply(INFO) != null, "limiting().apply");
        check(HttpResponse.BodyHandlers.ofPublisher().apply(INFO).getBody() != null,
                "ofPublisher().apply(...).getBody()");

        // ---- deepened: behavior through the full Flow cycle ----
        // discarding -> Void null
        HttpResponse.BodySubscriber<Void> disc = HttpResponse.BodyHandlers.discarding().apply(INFO);
        disc.onSubscribe(SUB);
        disc.onNext(List.of(bb("x")));
        disc.onComplete();
        check(disc.getBody().toCompletableFuture().get() == null, "discarding body is null");

        // replacing -> sentinel; replacing(null) -> null body
        HttpResponse.BodySubscriber<String> rep = HttpResponse.BodyHandlers.replacing("S").apply(INFO);
        rep.onSubscribe(SUB);
        rep.onComplete();
        check("S".equals(rep.getBody().toCompletableFuture().get()), "replacing body is the sentinel");
        HttpResponse.BodySubscriber<String> repNull =
                HttpResponse.BodyHandlers.<String>replacing(null).apply(INFO);
        repNull.onSubscribe(SUB);
        repNull.onComplete();
        check(repNull.getBody().toCompletableFuture().get() == null,
                "replacing(null) body is null (probed)");

        // ofString() default charset decodes UTF-8 text
        HttpResponse.BodySubscriber<String> str = HttpResponse.BodyHandlers.ofString().apply(INFO);
        str.onSubscribe(SUB);
        str.onNext(List.of(bb("he"), bb("llo")));
        str.onComplete();
        check("hello".equals(str.getBody().toCompletableFuture().get()),
                "ofString handler concatenates chunks");

        // ofByteArray
        HttpResponse.BodySubscriber<byte[]> barr = HttpResponse.BodyHandlers.ofByteArray().apply(INFO);
        barr.onSubscribe(SUB);
        barr.onNext(List.of(bb("ab"), bb("cd")));
        barr.onComplete();
        check(java.util.Arrays.equals(barr.getBody().toCompletableFuture().get(),
                new byte[]{'a', 'b', 'c', 'd'}), "ofByteArray handler bytes");

        // ofLines: cross-chunk line splitting
        HttpResponse.BodySubscriber<java.util.stream.Stream<String>> lines =
                HttpResponse.BodyHandlers.ofLines().apply(INFO);
        lines.onSubscribe(SUB);
        lines.onNext(List.of(bb("one\ntw"), bb("o\nthree")));
        lines.onComplete();
        try (java.util.stream.Stream<String> s = lines.getBody().toCompletableFuture().get()) {
            check(s.toList().equals(List.of("one", "two", "three")),
                    "ofLines handler splits across chunk boundaries");
        }

        // ofInputStream: readable stream of the body
        HttpResponse.BodySubscriber<java.io.InputStream> ins =
                HttpResponse.BodyHandlers.ofInputStream().apply(INFO);
        ins.onSubscribe(SUB);
        ins.onNext(List.of(bb("str")));
        ins.onComplete();
        check("str".equals(new String(
                        ins.getBody().toCompletableFuture().get().readAllBytes(), StandardCharsets.UTF_8)),
                "ofInputStream handler readable body");

        // ofByteArrayConsumer: chunks then Optional.empty() end marker (probed)
        List<Optional<byte[]>> consumed = new ArrayList<>();
        HttpResponse.BodySubscriber<Void> cons = HttpResponse.BodyHandlers
                .ofByteArrayConsumer((Consumer<Optional<byte[]>>) consumed::add).apply(INFO);
        cons.onSubscribe(SUB);
        cons.onNext(List.of(bb("a")));
        cons.onNext(List.of(bb("b")));
        cons.onComplete();
        check(consumed.size() == 3, "consumer sees 2 chunks + end marker");
        check(consumed.get(0).isPresent() && consumed.get(0).get().length == 1,
                "first chunk delivered");
        check(!consumed.get(2).isPresent(), "final marker is Optional.empty()");

        // fromLineSubscriber with finisher: lines as strings + finisher result
        final int[] lineCount = new int[1];
        Flow.Subscriber<String> ls = new Flow.Subscriber<>() {
            Flow.Subscription sub;
            public void onSubscribe(Flow.Subscription s) { sub = s; sub.request(1); }
            public void onNext(String l) { lineCount[0]++; sub.request(1); }
            public void onError(Throwable t) { }
            public void onComplete() { }
        };
        HttpResponse.BodySubscriber<String> fls = HttpResponse.BodyHandlers
                .fromLineSubscriber(ls, s -> "L" + lineCount[0], "\n").apply(INFO);
        fls.onSubscribe(SUB);
        fls.onNext(List.of(bb("x\ny")));
        fls.onComplete();
        check("L2".equals(fls.getBody().toCompletableFuture().get()),
                "fromLineSubscriber finisher counts lines");

        // ofFile: writes the body to the file (uses a scratch directory).
        File dir = new File("/tmp/net-I-BodyHandlers");
        dir.mkdirs();
        Path target = dir.toPath().resolve("out.txt");
        HttpResponse.BodySubscriber<Path> file =
                HttpResponse.BodyHandlers.ofFile(target).apply(INFO);
        file.onSubscribe(SUB);
        file.onNext(List.of(bb("file-body")));
        file.onComplete();
        check("file-body".equals(java.nio.file.Files.readString(target)),
                "ofFile handler writes body content");
        check(target.equals(file.getBody().toCompletableFuture().get()),
                "ofFile body is the target path");

        // buffering(handler, n): batched but identical result
        HttpResponse.BodySubscriber<String> buf = HttpResponse.BodyHandlers.buffering(
                HttpResponse.BodyHandlers.ofString(), 2).apply(INFO);
        buf.onSubscribe(SUB);
        buf.onNext(List.of(bb("1"), bb("2"), bb("3")));
        buf.onComplete();
        check("123".equals(buf.getBody().toCompletableFuture().get()), "buffering handler result");

        // limiting(handler, n): transparent below the limit
        HttpResponse.BodySubscriber<String> lim = HttpResponse.BodyHandlers.limiting(
                HttpResponse.BodyHandlers.ofString(), 10).apply(INFO);
        lim.onSubscribe(SUB);
        lim.onNext(List.of(bb("lim")));
        lim.onComplete();
        check("lim".equals(lim.getBody().toCompletableFuture().get()), "limiting handler result");

        // ---- deepened: negative matrix (probed) ----
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.buffering(null, 4),
                "buffering(null, n) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodyHandlers.buffering(HttpResponse.BodyHandlers.ofString(), 0),
                "buffering(h, 0) IAE");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofString(), -1),
                "limiting(h, -1) IAE");
        check(HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofString(), 0) != null,
                "limiting(h, 0) tolerated (probed)");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofString(null),
                "ofString(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofFile(null),
                "ofFile(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofFileDownload(null),
                "ofFileDownload(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofByteArrayConsumer(null),
                "ofByteArrayConsumer(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.fromSubscriber(null),
                "fromSubscriber(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.fromLineSubscriber(null),
                "fromLineSubscriber(null) NPE");

        // onError on the string handler: body fails with the given cause.
        HttpResponse.BodySubscriber<String> err = HttpResponse.BodyHandlers.ofString().apply(INFO);
        err.onSubscribe(SUB);
        Exception boom = new Exception("bh-boom");
        err.onError(boom);
        try {
            err.getBody().toCompletableFuture().get();
            check(false, "error must propagate");
        } catch (Exception e) {
            check(e.getCause() == boom, "onError cause propagated to getBody()");
        }

        java.nio.file.Files.deleteIfExists(target);
        dir.delete();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
