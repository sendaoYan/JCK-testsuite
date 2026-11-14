/*
 * @test
 * @summary java.net.http.HttpResponse.BodySubscribers conformance: static factories return non-null subscribers.
 * @run main BodySubscribersTest
 */

import java.io.File;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class BodySubscribersTest {
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

    static ByteBuffer bb(String s) { return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)); }

    /** A byte-list subscriber recording every list for fromSubscriber tests. */
    static class ByteSub implements Flow.Subscriber<List<ByteBuffer>> {
        int lists;
        final List<byte[]> data = new ArrayList<>();
        Flow.Subscription sub;
        public void onSubscribe(Flow.Subscription s) { sub = s; sub.request(Long.MAX_VALUE); }
        public void onNext(List<ByteBuffer> item) {
            lists++;
            for (ByteBuffer b : item) { byte[] a = new byte[b.remaining()]; b.get(a); data.add(a); }
        }
        public void onError(Throwable t) { }
        public void onComplete() { }
    }

    /** A line subscriber recording every decoded line. */
    static class LineSub implements Flow.Subscriber<String> {
        final List<String> lines = new ArrayList<>();
        Flow.Subscription sub;
        public void onSubscribe(Flow.Subscription s) { sub = s; sub.request(Long.MAX_VALUE); }
        public void onNext(String l) { lines.add(l); }
        public void onError(Throwable t) { }
        public void onComplete() { }
    }

    public static void main(String[] args) throws Exception {
        // ---------- factories: non-null ----------
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
        check(HttpResponse.BodySubscribers.ofByteArrayConsumer(opt -> { }) != null, "ofByteArrayConsumer");
        check(HttpResponse.BodySubscribers.fromSubscriber(new ByteSub()) != null, "fromSubscriber");
        check(HttpResponse.BodySubscribers.fromSubscriber(new ByteSub(), s -> "f") != null,
                "fromSubscriber finisher");
        check(HttpResponse.BodySubscribers.fromLineSubscriber(new LineSub()) != null, "fromLineSubscriber");
        check(HttpResponse.BodySubscribers.fromLineSubscriber(
                new LineSub(), s -> "f", StandardCharsets.UTF_8, "\n") != null,
                "fromLineSubscriber full");

        // ---------- negatives (probed on JDK-27) ----------
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.ofString(null),
                "ofString(null charset) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.ofFile(null),
                "ofFile(null) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodySubscribers.ofFile(null, java.nio.file.StandardOpenOption.WRITE),
                "ofFile(null, opts) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.ofByteArrayConsumer(null),
                "ofByteArrayConsumer(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.fromSubscriber(null),
                "fromSubscriber(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.fromSubscriber(null, s -> "x"),
                "fromSubscriber(null, f) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodySubscribers.fromSubscriber(new ByteSub(), null),
                "fromSubscriber(s, null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.fromLineSubscriber(null),
                "fromLineSubscriber(null) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodySubscribers.fromLineSubscriber(null, s -> "x",
                        StandardCharsets.UTF_8, "\n"),
                "fromLineSubscriber(null, f, cs, sep) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodySubscribers.fromLineSubscriber(new LineSub(), null,
                        StandardCharsets.UTF_8, "\n"),
                "fromLineSubscriber(s, null, cs, sep) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodySubscribers.fromLineSubscriber(new LineSub(), s -> "x",
                        null, "\n"),
                "fromLineSubscriber(s, f, null charset, sep) NPE");
        // Probed: a null line separator is tolerated (system default used).
        check(HttpResponse.BodySubscribers.fromLineSubscriber(
                new LineSub(), s -> "x", StandardCharsets.UTF_8, null) != null,
                "fromLineSubscriber(s, f, cs, null sep) tolerated (probed)");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.buffering(null, 4),
                "buffering(null, n) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodySubscribers.buffering(HttpResponse.BodySubscribers.ofByteArray(), 0),
                "buffering(s, 0) IAE 'must be greater than 0'");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodySubscribers.buffering(HttpResponse.BodySubscribers.ofByteArray(), -2),
                "buffering(s, -2) IAE");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.mapping(null, x -> x),
                "mapping(null, f) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofByteArray(), null),
                "mapping(s, null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodySubscribers.limiting(null, 4),
                "limiting(null, n) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodySubscribers.limiting(HttpResponse.BodySubscribers.ofByteArray(), -1),
                "limiting(s, -1) IAE 'capacity must not be negative'");
        check(HttpResponse.BodySubscribers.limiting(HttpResponse.BodySubscribers.ofByteArray(), 0) != null,
                "limiting(s, 0) accepted (probed: capacity 0 legal)");

        // ---------- ofString ----------
        HttpResponse.BodySubscriber<String> so = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        so.onSubscribe(SUB);
        so.onNext(List.of(bb("hel"), bb("lo")));
        so.onComplete();
        check("hello".equals(so.getBody().toCompletableFuture().get()),
                "ofString concatenates chunks across lists");
        HttpResponse.BodySubscriber<String> so16 =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_16BE);
        so16.onSubscribe(SUB);
        so16.onNext(List.of(ByteBuffer.wrap(new byte[]{0, 104, 0, 105})));
        so16.onComplete();
        check("hi".equals(so16.getBody().toCompletableFuture().get()),
                "ofString honors the requested charset");
        HttpResponse.BodySubscriber<String> soEmpty =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        soEmpty.onSubscribe(SUB);
        soEmpty.onComplete();
        check("".equals(soEmpty.getBody().toCompletableFuture().get()),
                "ofString with no data completes empty");

        // ---------- ofByteArray / discarding / replacing ----------
        HttpResponse.BodySubscriber<byte[]> bo = HttpResponse.BodySubscribers.ofByteArray();
        bo.onSubscribe(SUB);
        bo.onNext(List.of(bb("AB"), bb("CD")));
        bo.onComplete();
        check(Arrays.equals(bo.getBody().toCompletableFuture().get(), new byte[]{'A', 'B', 'C', 'D'}),
                "ofByteArray concatenates all lists");
        HttpResponse.BodySubscriber<Void> bd = HttpResponse.BodySubscribers.discarding();
        bd.onSubscribe(SUB);
        bd.onNext(List.of(bb("junk")));
        bd.onComplete();
        check(bd.getBody().toCompletableFuture().get() == null, "discarding yields null");
        HttpResponse.BodySubscriber<String> br = HttpResponse.BodySubscribers.replacing("SENTINEL");
        br.onSubscribe(SUB);
        br.onNext(List.of(bb("ignored")));
        br.onComplete();
        check("SENTINEL".equals(br.getBody().toCompletableFuture().get()),
                "replacing ignores the delivered data");
        HttpResponse.BodySubscriber<String> brNull =
                HttpResponse.BodySubscribers.<String>replacing(null);
        brNull.onSubscribe(SUB);
        brNull.onComplete();
        check(brNull.getBody().toCompletableFuture().get() == null,
                "replacing(null) body is null (probed)");

        // ---------- mapping ----------
        HttpResponse.BodySubscriber<Integer> bm = HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofByteArray(), b -> b.length);
        bm.onSubscribe(SUB);
        bm.onNext(List.of(bb("12345")));
        bm.onComplete();
        check(bm.getBody().toCompletableFuture().get() == 5,
                "mapping transforms the upstream body");
        HttpResponse.BodySubscriber<String> bmChain = HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.mapping(
                        HttpResponse.BodySubscribers.ofByteArray(),
                        b -> new String(b, StandardCharsets.UTF_8)),
                s -> s + "!");
        bmChain.onSubscribe(SUB);
        bmChain.onNext(List.of(bb("x")));
        bmChain.onComplete();
        check("x!".equals(bmChain.getBody().toCompletableFuture().get()),
                "mapping is composable");

        // ---------- ofLines ----------
        HttpResponse.BodySubscriber<java.util.stream.Stream<String>> bl =
                HttpResponse.BodySubscribers.ofLines(StandardCharsets.UTF_8);
        bl.onSubscribe(SUB);
        bl.onNext(List.of(bb("one\ntw"), bb("o\nthree\n")));
        bl.onComplete();
        try (java.util.stream.Stream<String> st = bl.getBody().toCompletableFuture().get()) {
            check(st.toList().equals(List.of("one", "two", "three")),
                    "ofLines splits across chunk boundaries");
        }
        HttpResponse.BodySubscriber<java.util.stream.Stream<String>> blEmpty =
                HttpResponse.BodySubscribers.ofLines(StandardCharsets.UTF_8);
        blEmpty.onSubscribe(SUB);
        blEmpty.onComplete();
        try (java.util.stream.Stream<String> st = blEmpty.getBody().toCompletableFuture().get()) {
            check(st.toList().isEmpty(), "ofLines with no data yields an empty stream");
        }

        // ---------- ofInputStream ----------
        HttpResponse.BodySubscriber<java.io.InputStream> bi =
                HttpResponse.BodySubscribers.ofInputStream();
        bi.onSubscribe(SUB);
        bi.onNext(List.of(bb("stream")));
        bi.onComplete();
        check("stream".equals(new String(
                bi.getBody().toCompletableFuture().get().readAllBytes(), StandardCharsets.UTF_8)),
                "ofInputStream exposes the body bytes");

        // ---------- ofByteArrayConsumer ----------
        List<Optional<byte[]>> consumed = new ArrayList<>();
        HttpResponse.BodySubscriber<Void> bc = HttpResponse.BodySubscribers.ofByteArrayConsumer(
                (Consumer<Optional<byte[]>>) consumed::add);
        bc.onSubscribe(SUB);
        bc.onNext(List.of(bb("ab")));
        bc.onNext(List.of(bb("cd")));
        bc.onComplete();
        check(consumed.size() == 3, "ofByteArrayConsumer: 2 data callbacks + 1 end marker");
        check(Arrays.equals(consumed.get(0).get(), new byte[]{'a', 'b'}), "first chunk bytes");
        check(Arrays.equals(consumed.get(1).get(), new byte[]{'c', 'd'}), "second chunk bytes");
        check(!consumed.get(2).isPresent(), "end marker is Optional.empty()");

        // ---------- fromSubscriber / fromLineSubscriber ----------
        ByteSub bs = new ByteSub();
        HttpResponse.BodySubscriber<Void> fs = HttpResponse.BodySubscribers.fromSubscriber(bs);
        fs.onSubscribe(SUB);
        fs.onNext(List.of(bb("a"), bb("b")));
        fs.onComplete();
        check(bs.lists == 1, "fromSubscriber forwards each list");
        check(bs.data.size() == 2, "fromSubscriber preserves each buffer in the list");
        check(fs.getBody().toCompletableFuture().get() == null,
                "fromSubscriber without finisher yields null body");

        ByteSub bs2 = new ByteSub();
        HttpResponse.BodySubscriber<String> fs2 =
                HttpResponse.BodySubscribers.fromSubscriber(bs2, s -> "lists:" + s.lists);
        fs2.onSubscribe(SUB);
        fs2.onNext(List.of(bb("x")));
        fs2.onComplete();
        check("lists:1".equals(fs2.getBody().toCompletableFuture().get()),
                "fromSubscriber finisher applied after completion (probed)");

        LineSub ls = new LineSub();
        HttpResponse.BodySubscriber<Void> fls = HttpResponse.BodySubscribers.fromLineSubscriber(ls);
        fls.onSubscribe(SUB);
        fls.onNext(List.of(bb("l1\nl2")));
        fls.onComplete();
        check(ls.lines.equals(List.of("l1", "l2")),
                "fromLineSubscriber decodes lines (default charset/separator)");
        check(fls.getBody().toCompletableFuture().get() == null,
                "fromLineSubscriber without finisher yields null body");

        LineSub ls2 = new LineSub();
        HttpResponse.BodySubscriber<String> fls2 = HttpResponse.BodySubscribers.fromLineSubscriber(
                ls2, s -> "lines:" + s.lines.size(), StandardCharsets.UTF_8, "\n");
        fls2.onSubscribe(SUB);
        fls2.onNext(List.of(bb("a\nb\nc")));
        fls2.onComplete();
        check("lines:3".equals(fls2.getBody().toCompletableFuture().get()),
                "fromLineSubscriber finisher counts lines");
        check(ls2.lines.equals(List.of("a", "b", "c")), "fromLineSubscriber line contents");

        // ---------- ofFile ----------
        // Absolute java.io.tmpdir ("/tmp" is drive-relative on Windows) plus a
        // per-run suffix: concurrent configurations of this test must not
        // share fixtures.
        File dir = new File(System.getProperty("java.io.tmpdir"),
                "net-I-BodySubscribers-" + System.nanoTime());
        dir.mkdirs();
        Path target = dir.toPath().resolve("out.bin");
        HttpResponse.BodySubscriber<Path> bf = HttpResponse.BodySubscribers.ofFile(target);
        bf.onSubscribe(SUB);
        bf.onNext(List.of(bb("file-body")));
        bf.onComplete();
        check("file-body".equals(java.nio.file.Files.readString(target)),
                "ofFile writes the delivered body");
        check(target.equals(bf.getBody().toCompletableFuture().get()), "ofFile body is the path");

        // ---------- ofPublisher ----------
        HttpResponse.BodySubscriber<Flow.Publisher<List<ByteBuffer>>> bp =
                HttpResponse.BodySubscribers.ofPublisher();
        bp.onSubscribe(SUB);
        bp.onComplete();
        Flow.Publisher<List<ByteBuffer>> pub = bp.getBody().toCompletableFuture().get();
        check(pub != null, "ofPublisher yields a publisher");
        final List<Integer> sizes = new ArrayList<>();
        final CompletableFuture<Void> done = new CompletableFuture<>();
        pub.subscribe(new Flow.Subscriber<List<ByteBuffer>>() {
            Flow.Subscription sub;
            public void onSubscribe(Flow.Subscription s) { sub = s; sub.request(1); }
            public void onNext(List<ByteBuffer> item) {
                sizes.add(item.size());
                sub.request(1);
            }
            public void onError(Throwable t) { done.completeExceptionally(t); }
            public void onComplete() { done.complete(null); }
        });
        done.get();
        check(sizes.isEmpty(), "ofPublisher with no data emits nothing then completes");

        // With data: the downstream subscriber must attach (and request) before the
        // data is delivered; demand is forwarded upstream (probed).
        final java.util.concurrent.atomic.AtomicLong demanded =
                new java.util.concurrent.atomic.AtomicLong();
        Flow.Subscription upstream = new Flow.Subscription() {
            public void request(long n) { demanded.addAndGet(n); }
            public void cancel() { }
        };
        HttpResponse.BodySubscriber<Flow.Publisher<List<ByteBuffer>>> bp2 =
                HttpResponse.BodySubscribers.ofPublisher();
        bp2.onSubscribe(upstream);
        Flow.Publisher<List<ByteBuffer>> pub2 = bp2.getBody().toCompletableFuture().get();
        final StringBuilder text = new StringBuilder();
        final CompletableFuture<Void> done2 = new CompletableFuture<>();
        final java.util.concurrent.atomic.AtomicReference<Throwable> secondError =
                new java.util.concurrent.atomic.AtomicReference<>();
        pub2.subscribe(new Flow.Subscriber<List<ByteBuffer>>() {
            Flow.Subscription sub;
            public void onSubscribe(Flow.Subscription s) { sub = s; sub.request(1); }
            public void onNext(List<ByteBuffer> item) {
                for (ByteBuffer b : item) { byte[] a = new byte[b.remaining()]; b.get(a); text.append(new String(a, StandardCharsets.UTF_8)); }
                sub.request(1);
            }
            public void onError(Throwable t) { done2.completeExceptionally(t); }
            public void onComplete() { done2.complete(null); }
        });
        bp2.onNext(List.of(bb("p1"), bb("p2")));
        bp2.onNext(List.of(bb("q")));
        bp2.onComplete();
        done2.get();
        check("p1p2q".equals(text.toString()),
                "ofPublisher forwards delivered lists to the attached subscriber");
        check(demanded.get() == 3L,
                "ofPublisher forwards downstream demand upstream (1 + 2 re-requests, probed)");
        // A second subscriber is rejected per Flow.Publisher semantics.
        pub2.subscribe(new Flow.Subscriber<List<ByteBuffer>>() {
            public void onSubscribe(Flow.Subscription s) { }
            public void onNext(List<ByteBuffer> item) { }
            public void onError(Throwable t) { secondError.set(t); }
            public void onComplete() { }
        });
        check(secondError.get() instanceof IllegalStateException,
                "second subscriber receives onError ISE (probed)");

        // ---------- buffering / limiting ----------
        HttpResponse.BodySubscriber<String> bbuf = HttpResponse.BodySubscribers.buffering(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), 8);
        bbuf.onSubscribe(SUB);
        bbuf.onNext(List.of(bb("abc")));
        bbuf.onComplete();
        check("abc".equals(bbuf.getBody().toCompletableFuture().get()), "buffering transparent result");

        HttpResponse.BodySubscriber<String> blim = HttpResponse.BodySubscribers.limiting(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), 64);
        blim.onSubscribe(SUB);
        blim.onNext(List.of(bb("lim")));
        blim.onComplete();
        check("lim".equals(blim.getBody().toCompletableFuture().get()), "limiting transparent result");

        // ---------- error propagation ----------
        HttpResponse.BodySubscriber<String> berr =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        berr.onSubscribe(SUB);
        Exception boom = new Exception("boom");
        berr.onError(boom);
        try {
            berr.getBody().toCompletableFuture().get();
            check(false, "ofString error must fail the body");
        } catch (Exception e) {
            check(e.getCause() == boom, "exact cause relayed");
        }
        HttpResponse.BodySubscriber<Integer> mapErr = HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofByteArray(), b -> b.length);
        mapErr.onSubscribe(SUB);
        mapErr.onError(boom);
        try {
            mapErr.getBody().toCompletableFuture().get();
            check(false, "mapping error must fail the body");
        } catch (Exception e) {
            check(e.getCause() == boom, "mapping relays the cause");
        }

        java.nio.file.Files.deleteIfExists(target);
        dir.delete();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
