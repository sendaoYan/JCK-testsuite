/*
 * @test
 * @summary java.net.http.HttpRequest.BodyPublisher conformance: interface extends Flow.Publisher.
 * @run main BodyPublisherTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

public class BodyPublisherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal collecting Flow.Subscriber: echoes demand and records every item. */
    static class Collector implements Flow.Subscriber<ByteBuffer> {
        Flow.Subscription subscription;
        final List<byte[]> items = new ArrayList<>();
        final AtomicInteger onNext = new AtomicInteger();
        final AtomicInteger onComplete = new AtomicInteger();
        final AtomicInteger onError = new AtomicInteger();
        final AtomicInteger onSubscribe = new AtomicInteger();
        volatile Throwable error;

        public void onSubscribe(Flow.Subscription s) { onSubscribe.incrementAndGet(); subscription = s; }
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

    static Collector drain(HttpRequest.BodyPublisher p, long initialDemand) {
        Collector c = new Collector();
        p.subscribe(c);
        c.subscription.request(initialDemand);
        return c;
    }

    static String text(Collector c) {
        StringBuilder sb = new StringBuilder();
        for (byte[] a : c.items) sb.append(new String(a, StandardCharsets.ISO_8859_1));
        return sb.toString();
    }

    public static void main(String[] args) {
        check(HttpRequest.BodyPublisher.class.isInterface(), "BodyPublisher is an interface");
        check(Flow.Publisher.class.isAssignableFrom(HttpRequest.BodyPublisher.class),
                "BodyPublisher extends Flow.Publisher");

        HttpRequest.BodyPublisher p = HttpRequest.BodyPublishers.ofString("hello");
        check(p != null, "ofString non-null");
        check(p instanceof Flow.Publisher, "ofString is a Flow.Publisher");
        check(p.contentLength() == 5, "ofString('hello') contentLength 5");

        // ---- deepened: interface shape (javap-verified) ----
        try {
            Method cl = HttpRequest.BodyPublisher.class.getMethod("contentLength");
            check(cl.getReturnType() == long.class, "contentLength() returns long");
            check(Modifier.isAbstract(cl.getModifiers()), "contentLength is abstract");
            Method sub = HttpRequest.BodyPublisher.class.getMethod("subscribe",
                    Flow.Subscriber.class);
            check(Modifier.isAbstract(sub.getModifiers()), "subscribe is abstract (from Flow.Publisher)");
        } catch (NoSuchMethodException e) {
            check(false, "unexpected: " + e);
        }
        // The interface adds exactly one method to Flow.Publisher.
        int declared = HttpRequest.BodyPublisher.class.getDeclaredMethods().length;
        check(declared == 1, "BodyPublisher declares exactly one method (contentLength)");

        // ---- deepened: Flow delivery semantics (probed: synchronous on request) ----
        Collector hi = drain(HttpRequest.BodyPublishers.ofString("hi"), 1);
        check(hi.onSubscribe.get() == 1, "onSubscribe invoked exactly once");
        check(hi.onNext.get() == 1, "ofString('hi') delivers a single onNext");
        check(hi.onComplete.get() == 1, "onComplete delivered synchronously");
        check(hi.onError.get() == 0, "no onError on the happy path");
        check("hi".equals(text(hi)), "ofString('hi') bytes are exactly 'h','i'");

        // Over-requesting still delivers one item then completion.
        Collector many = drain(HttpRequest.BodyPublishers.ofString("hi"), 100);
        check(many.onNext.get() == 1 && many.onComplete.get() == 1,
                "request(100) still one onNext then onComplete");

        // noBody() completes immediately with no items.
        Collector none = drain(HttpRequest.BodyPublishers.noBody(), 1);
        check(none.onNext.get() == 0, "noBody() delivers no items");
        check(none.onComplete.get() == 1, "noBody() completes");

        // ofByteArray delivers the exact bytes.
        Collector ba = drain(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}), 1);
        check(ba.items.size() == 1 && ba.items.get(0).length == 3, "ofByteArray single item of 3 bytes");
        check(ba.items.get(0)[0] == 1 && ba.items.get(0)[1] == 2 && ba.items.get(0)[2] == 3,
                "ofByteArray bytes preserved");

        // Two independent subscriptions of the same publisher each see the data.
        HttpRequest.BodyPublisher twice = HttpRequest.BodyPublishers.ofString("xy");
        Collector c1 = new Collector(), c2 = new Collector();
        twice.subscribe(c1);
        twice.subscribe(c2);
        c1.subscription.request(1);
        c2.subscription.request(1);
        check(c1.onNext.get() == 1 && c2.onNext.get() == 1, "independent subscriptions each deliver");
        check("xy".equals(text(c1)) && "xy".equals(text(c2)), "both subscriptions see identical bytes");

        // Charset affects the delivered bytes: UTF-16BE "hi" == 00 68 00 69.
        Collector u16 = drain(HttpRequest.BodyPublishers.ofString("hi", StandardCharsets.UTF_16BE), 1);
        byte[] expected = new byte[]{0, 104, 0, 105};
        check(u16.items.size() == 1 && java.util.Arrays.equals(u16.items.get(0), expected),
                "UTF_16BE bytes are 00 68 00 69");

        // ofByteArrays preserves the iterable's chunk order.
        Collector obs = drain(HttpRequest.BodyPublishers.ofByteArrays(
                java.util.Arrays.asList(new byte[]{10, 11}, new byte[]{12})), 1);
        check(obs.onNext.get() == 2, "ofByteArrays delivers one item per chunk");
        check(obs.items.get(0).length == 2 && obs.items.get(1).length == 1,
                "ofByteArrays chunk sizes preserved");
        check(obs.items.get(0)[0] == 10 && obs.items.get(0)[1] == 11 && obs.items.get(1)[0] == 12,
                "ofByteArrays byte order preserved");

        // ---- deepened: demand edge cases (probed on JDK-27) ----
        // Non-positive demand is reported through onError with an
        // IllegalArgumentException "non-positive subscription request: N";
        // nothing is thrown synchronously and no items are delivered.
        Collector zero = drain(HttpRequest.BodyPublishers.ofString("hi"), 0);
        check(zero.onNext.get() == 0 && zero.onComplete.get() == 0,
                "request(0) delivers no items and no completion");
        check(zero.onError.get() == 1, "request(0) signals onError (probed)");
        check(zero.error instanceof IllegalArgumentException, "request(0) error is an IAE");
        check(zero.error.getMessage() != null && zero.error.getMessage().contains("non-positive"),
                "request(0) IAE message mentions non-positive");
        check(zero.onSubscribe.get() == 1, "request(0) still registers onSubscribe");
        Collector neg = drain(HttpRequest.BodyPublishers.ofString("hi"), -1);
        check(neg.onNext.get() == 0 && neg.onError.get() == 1,
                "request(-1) signals onError without delivery (probed)");
        check(neg.error instanceof IllegalArgumentException, "request(-1) error is an IAE");

        // ---- deepened: the interface is implementable by user code (not sealed) ----
        check(!HttpRequest.BodyPublisher.class.isSealed(), "BodyPublisher is not sealed");
        HttpRequest.BodyPublisher custom = new HttpRequest.BodyPublisher() {
            public long contentLength() { return 42L; }
            public void subscribe(Flow.Subscriber<? super ByteBuffer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    public void request(long n) { }
                    public void cancel() { }
                });
            }
        };
        check(custom.contentLength() == 42L, "user-implemented BodyPublisher honored");

        // Empty string publisher: length 0, immediate completion, no items.
        HttpRequest.BodyPublisher empty = HttpRequest.BodyPublishers.ofString("");
        check(empty.contentLength() == 0L, "ofString('') length 0");
        Collector ec = drain(empty, 1);
        check(ec.onNext.get() == 0 && ec.onComplete.get() == 1, "ofString('') completes without items");

        // contentLength is consistent with the delivered bytes for ofString.
        check(HttpRequest.BodyPublishers.ofString("hello").contentLength() == text(
                drain(HttpRequest.BodyPublishers.ofString("hello"), 1)).length(),
                "contentLength matches delivered byte count");

        // A subscriber that cancels: no further delivery, no error (cancel is legal any time).
        Collector canceller = new Collector() {
            public void onSubscribe(Flow.Subscription s) {
                super.onSubscribe(s);
                s.cancel();
            }
        };
        HttpRequest.BodyPublishers.ofString("q").subscribe(canceller);
        check(canceller.onNext.get() == 0, "cancelled subscription receives no items");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
