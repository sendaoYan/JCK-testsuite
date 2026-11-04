/*
 * @test
 * @summary java.net.http.HttpRequest.BodyPublishers conformance: static factory content lengths.
 * @run main BodyPublishersTest
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

public class BodyPublishersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expect(Class<? extends Throwable> ex, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    /** Collecting subscriber: drains the whole body with unit demand. */
    static class Collector implements Flow.Subscriber<ByteBuffer> {
        Flow.Subscription subscription;
        final List<byte[]> items = new ArrayList<>();
        final AtomicInteger onNext = new AtomicInteger();
        final AtomicInteger onComplete = new AtomicInteger();
        final AtomicInteger onError = new AtomicInteger();
        volatile Throwable error;
        public void onSubscribe(Flow.Subscription s) { subscription = s; }
        public void onNext(ByteBuffer b) {
            onNext.incrementAndGet();
            byte[] a = new byte[b.remaining()];
            b.get(a);
            items.add(a);
            subscription.request(1);
        }
        public void onError(Throwable t) { onError.incrementAndGet(); error = t; }
        public void onComplete() { onComplete.incrementAndGet(); }
    }

    static Collector drain(HttpRequest.BodyPublisher p) {
        Collector c = new Collector();
        p.subscribe(c);
        c.subscription.request(1);
        return c;
    }

    static byte[] bytes(Collector c) {
        int n = 0;
        for (byte[] a : c.items) n += a.length;
        byte[] out = new byte[n];
        int i = 0;
        for (byte[] a : c.items) { System.arraycopy(a, 0, out, i, a.length); i += a.length; }
        return out;
    }

    public static void main(String[] args) throws Exception {
        // ---------- ofString ----------
        check(HttpRequest.BodyPublishers.ofString("abc").contentLength() == 3, "ofString length");
        check(HttpRequest.BodyPublishers.ofString("abc", StandardCharsets.UTF_8).contentLength() == 3,
                "ofString with charset length");
        check(HttpRequest.BodyPublishers.ofString("").contentLength() == 0, "ofString('') length 0");
        // Charset-driven length (probed): UTF-16 prepends a 2-byte BOM -> "abc" is 8 bytes;
        // explicit BE/LE variants are exactly 2*N bytes without BOM.
        check(HttpRequest.BodyPublishers.ofString("abc", StandardCharsets.UTF_16).contentLength() == 8,
                "ofString(UTF_16) length 8 (BOM included, probed)");
        check(HttpRequest.BodyPublishers.ofString("abc", StandardCharsets.UTF_16BE).contentLength() == 6,
                "ofString(UTF_16BE) length 6 (no BOM)");
        check(HttpRequest.BodyPublishers.ofString("abc", StandardCharsets.UTF_16LE).contentLength() == 6,
                "ofString(UTF_16LE) length 6 (no BOM)");
        check(HttpRequest.BodyPublishers.ofString("hi", StandardCharsets.ISO_8859_1).contentLength() == 2,
                "ofString(ISO_8859_1) length 2");
        check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.ofString("hi", StandardCharsets.UTF_16BE))),
                new byte[]{0, 104, 0, 105}), "ofString(UTF_16BE) exact bytes 00 68 00 69");
        expect(NullPointerException.class, () -> HttpRequest.BodyPublishers.ofString(null),
                "ofString(null) NPE");
        expect(NullPointerException.class, () -> HttpRequest.BodyPublishers.ofString("x", null),
                "ofString(x, null charset) NPE");

        // ---------- ofByteArray ----------
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}).contentLength() == 3,
                "ofByteArray length");
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[0]).contentLength() == 0,
                "ofByteArray(empty) length 0");
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3, 4}, 1, 2).contentLength() == 2,
                "ofByteArray with offset length");
        check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}, 1, 2))),
                new byte[]{2, 3}), "ofByteArray(offset) delivers the slice bytes");
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[]{9}, 1, 0).contentLength() == 0,
                "ofByteArray(len 0 at end) accepted (probed)");
        expect(NullPointerException.class, () -> HttpRequest.BodyPublishers.ofByteArray(null),
                "ofByteArray(null) NPE");
        // Bounds violations raise IndexOutOfBoundsException (probed, not IAE).
        expect(IndexOutOfBoundsException.class,
                () -> HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}, -1, 1),
                "ofByteArray(negative offset) IndexOutOfBounds");
        expect(IndexOutOfBoundsException.class,
                () -> HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}, 0, 4),
                "ofByteArray(length past end) IndexOutOfBounds");
        expect(IndexOutOfBoundsException.class,
                () -> HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}, 2, -1),
                "ofByteArray(negative length) IndexOutOfBounds");
        expect(IndexOutOfBoundsException.class,
                () -> HttpRequest.BodyPublishers.ofByteArray(new byte[]{1}, 2, 0),
                "ofByteArray(offset past end) IndexOutOfBounds");

        // ---------- noBody ----------
        check(HttpRequest.BodyPublishers.noBody().contentLength() == 0, "noBody length 0");
        check(HttpRequest.BodyPublishers.noBody() != null, "noBody non-null");
        Collector nb = drain(HttpRequest.BodyPublishers.noBody());
        check(nb.onNext.get() == 0 && nb.onComplete.get() == 1, "noBody completes without items");

        // ---------- ofInputStream ----------
        check(HttpRequest.BodyPublishers.ofInputStream(
                () -> new ByteArrayInputStream(new byte[]{1, 2})).contentLength() == -1,
                "ofInputStream unknown length -1");
        expect(NullPointerException.class, () -> HttpRequest.BodyPublishers.ofInputStream(null),
                "ofInputStream(null) NPE");

        // ---------- ofByteArrays ----------
        // ofByteArrays always reports an unknown length: the iterable is not
        // pre-measured (probed: -1 even for a fixed two-element list).
        check(HttpRequest.BodyPublishers.ofByteArrays(
                Arrays.asList(new byte[]{1}, new byte[]{2, 3})).contentLength() == -1,
                "ofByteArrays unknown length -1 (probed)");
        check(HttpRequest.BodyPublishers.ofByteArrays(List.<byte[]>of()).contentLength() == -1,
                "ofByteArrays(empty) unknown length -1 (probed)");
        check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.ofByteArrays(
                        Arrays.asList(new byte[]{10, 11}, new byte[]{12})))),
                new byte[]{10, 11, 12}), "ofByteArrays preserves chunk and byte order");
        expect(NullPointerException.class, () -> HttpRequest.BodyPublishers.ofByteArrays(null),
                "ofByteArrays(null) NPE");
        // A null element is accepted by the factory (deferred; probed) - only length checked here.
        check(HttpRequest.BodyPublishers.ofByteArrays(
                Arrays.asList(new byte[]{1}, null)).contentLength() == -1,
                "ofByteArrays(null element) factory-tolerated, length unknown (probed)");

        // ---------- fromPublisher ----------
        check(HttpRequest.BodyPublishers.fromPublisher(
                HttpRequest.BodyPublishers.noBody()).contentLength() == -1,
                "fromPublisher unknown length -1");
        check(HttpRequest.BodyPublishers.fromPublisher(
                HttpRequest.BodyPublishers.noBody(), 7).contentLength() == 7,
                "fromPublisher(p, 7) contentLength 7");
        expect(NullPointerException.class,
                () -> HttpRequest.BodyPublishers.fromPublisher(null), "fromPublisher(null) NPE");
        expect(NullPointerException.class,
                () -> HttpRequest.BodyPublishers.fromPublisher(null, 1), "fromPublisher(null, 1) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.BodyPublishers.fromPublisher(HttpRequest.BodyPublishers.noBody(), 0),
                "fromPublisher(p, 0) IAE non-positive contentLength (probed)");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.BodyPublishers.fromPublisher(HttpRequest.BodyPublishers.noBody(), -3),
                "fromPublisher(p, -3) IAE non-positive contentLength");
        // Delegation: a known-length wrapper still delivers the underlying bytes.
        check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.fromPublisher(
                        HttpRequest.BodyPublishers.ofString("hi"), 2))),
                new byte[]{'h', 'i'}), "fromPublisher delegates data flow");

        // ---------- ofFile / ofFileChannel ----------
        File dir = new File("/tmp/net-I-BodyPublishers");
        dir.mkdirs();
        File f = new File(dir, "sample.bin");
        try (FileOutputStream fo = new FileOutputStream(f)) {
            fo.write(new byte[]{7, 8, 9});
        }
        check(HttpRequest.BodyPublishers.ofFile(f.toPath()).contentLength() == 3,
                "ofFile(real file) length equals file size");
        check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.ofFile(f.toPath()))),
                new byte[]{7, 8, 9}), "ofFile delivers file contents");
        expect(NullPointerException.class, () -> HttpRequest.BodyPublishers.ofFile(null),
                "ofFile(null) NPE");
        expect(java.io.FileNotFoundException.class,
                () -> HttpRequest.BodyPublishers.ofFile(
                        dir.toPath().resolve("definitely-missing-" + System.nanoTime() + ".bin")),
                "ofFile(missing) throws FileNotFoundException at factory time (probed)");
        try (FileChannel ch = FileChannel.open(f.toPath())) {
            check(HttpRequest.BodyPublishers.ofFileChannel(ch, 0, 3).contentLength() == 3,
                    "ofFileChannel(ch, 0, 3) length 3");
            check(HttpRequest.BodyPublishers.ofFileChannel(ch, 1, 1).contentLength() == 1,
                    "ofFileChannel(ch, 1, 1) length 1");
            check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.ofFileChannel(ch, 1, 2))),
                    new byte[]{8, 9}), "ofFileChannel delivers the requested window");
        }
        expect(NullPointerException.class, () -> HttpRequest.BodyPublishers.ofFileChannel(null, 0, 0),
                "ofFileChannel(null channel) NPE 'channel' (probed)");

        // ---------- concat (JDK-27 API) ----------
        check(HttpRequest.BodyPublishers.concat().contentLength() == 0,
                "concat() with no publishers has length 0 (probed)");
        check(HttpRequest.BodyPublishers.concat(HttpRequest.BodyPublishers.noBody()).contentLength() == 0,
                "concat(noBody) length 0");
        check(HttpRequest.BodyPublishers.concat(
                        HttpRequest.BodyPublishers.ofString("ab"),
                        HttpRequest.BodyPublishers.ofByteArray(new byte[]{3})).contentLength() == 3,
                "concat sums the lengths");
        check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.concat(
                        HttpRequest.BodyPublishers.ofString("ab"),
                        HttpRequest.BodyPublishers.ofByteArray(new byte[]{3})))),
                new byte[]{'a', 'b', 3}), "concat preserves the concatenation order");
        check(Arrays.equals(bytes(drain(HttpRequest.BodyPublishers.concat(
                        HttpRequest.BodyPublishers.ofString("x"),
                        HttpRequest.BodyPublishers.noBody(),
                        HttpRequest.BodyPublishers.ofString("y")))),
                new byte[]{'x', 'y'}), "concat skips empty publishers' content");
        expect(NullPointerException.class,
                () -> HttpRequest.BodyPublishers.concat((HttpRequest.BodyPublisher[]) null),
                "concat(null array) NPE");
        // concat with a null element: factory accepts, length -1 via unknown components? (probed: NPE deferred)
        check(HttpRequest.BodyPublishers.concat(HttpRequest.BodyPublishers.ofString("q")).contentLength() == 1,
                "concat(single) passthrough");

        HttpRequest.BodyPublisher p = HttpRequest.BodyPublishers.ofString("hello");
        check(p instanceof HttpRequest.BodyPublisher, "ofString returns a BodyPublisher");

        // ---------- deferred validation via Flow (probed) ----------
        // ofByteArrays with a null element: earlier chunks deliver, then onError NPE.
        Collector nullElem = new Collector();
        HttpRequest.BodyPublishers.ofByteArrays(Arrays.asList(new byte[]{1}, null)).subscribe(nullElem);
        nullElem.subscription.request(1);
        check(nullElem.onNext.get() == 1, "ofByteArrays(null elem) delivers chunks before the null");
        check(nullElem.onError.get() == 1 && nullElem.error instanceof NullPointerException,
                "ofByteArrays(null elem) reports NPE through onError (probed)");
        check(nullElem.onComplete.get() == 0, "ofByteArrays(null elem) never completes");

        // ofInputStream with a supplier returning null: onError IOException (probed).
        Collector nullSup = new Collector();
        HttpRequest.BodyPublishers.ofInputStream(() -> null).subscribe(nullSup);
        nullSup.subscription.request(1);
        check(nullSup.onError.get() == 1 && nullSup.error instanceof java.io.IOException,
                "ofInputStream(null supplier) reports IOException through onError");
        check(nullSup.onComplete.get() == 0, "ofInputStream(null supplier) never completes");
        // ofInputStream streams its bytes lazily at subscription time.
        Collector lazy = drain(HttpRequest.BodyPublishers.ofInputStream(
                () -> new ByteArrayInputStream(new byte[]{5, 6})));
        check(Arrays.equals(bytes(lazy), new byte[]{5, 6}), "ofInputStream delivers the stream content");
        check(lazy.onComplete.get() == 1, "ofInputStream completes after draining");

        // concat propagates unknown length if any member is unknown (probed).
        check(HttpRequest.BodyPublishers.concat(
                        HttpRequest.BodyPublishers.ofString("ab"),
                        HttpRequest.BodyPublishers.ofByteArrays(Arrays.asList(new byte[]{1})))
                        .contentLength() == -1,
                "concat with an unknown-length member reports -1 (probed)");
        check(HttpRequest.BodyPublishers.concat(
                        HttpRequest.BodyPublishers.ofByteArray(new byte[]{1}),
                        HttpRequest.BodyPublishers.ofByteArray(new byte[]{2, 3}))
                        .contentLength() == 3,
                "concat of known members sums to 3");

        // ofFileChannel rejects a window outside the channel size at factory time (probed).
        try (FileChannel ch = FileChannel.open(f.toPath())) {
            expect(IndexOutOfBoundsException.class,
                    () -> HttpRequest.BodyPublishers.ofFileChannel(ch, 0, 10),
                    "ofFileChannel(window past EOF) IndexOutOfBounds (probed)");
        }

        // ofFile supports several independent subscriptions (each re-reads the file).
        Collector fa = new Collector(), fb = new Collector();
        HttpRequest.BodyPublishers.ofFile(f.toPath()).subscribe(fa);
        HttpRequest.BodyPublishers.ofFile(f.toPath()).subscribe(fb);
        fa.subscription.request(1);
        fb.subscription.request(1);
        check(fa.onComplete.get() == 1 && fb.onComplete.get() == 1,
                "ofFile two independent subscriptions both complete (probed)");
        check(Arrays.equals(bytes(fa), new byte[]{7, 8, 9}) && Arrays.equals(bytes(fb), new byte[]{7, 8, 9}),
                "ofFile both subscriptions read identical content");

        // cleanup
        f.delete();
        dir.delete();

        // cleanup
        f.delete();
        dir.delete();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
