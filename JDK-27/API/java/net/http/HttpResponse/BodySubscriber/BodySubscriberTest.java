/*
 * @test
 * @summary java.net.http.HttpResponse.BodySubscriber conformance: interface extends Flow.Subscriber.
 * @run main BodySubscriberTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

public class BodySubscriberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final Flow.Subscription SUB = new Flow.Subscription() {
        public void request(long n) { }
        public void cancel() { }
    };

    static ByteBuffer bb(String s) { return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)); }

    public static void main(String[] args) throws Exception {
        check(HttpResponse.BodySubscriber.class.isInterface(), "BodySubscriber is an interface");
        check(Flow.Subscriber.class.isAssignableFrom(HttpResponse.BodySubscriber.class),
                "BodySubscriber extends Flow.Subscriber");
        check(HttpResponse.BodySubscriber.class.getTypeParameters().length == 1,
                "BodySubscriber has one type parameter");
        check(!HttpResponse.BodySubscriber.class.isSealed(), "BodySubscriber is user-implementable");

        HttpResponse.BodySubscriber<String> s =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        check(s != null, "ofString non-null");
        check(s instanceof Flow.Subscriber, "ofString is a Flow.Subscriber");
        check(s.getBody() != null, "getBody() non-null");
        check(s.getBody() instanceof CompletionStage, "getBody() returns a CompletionStage");

        // ---- deepened: interface shape ----
        Method getBody;
        try {
            getBody = HttpResponse.BodySubscriber.class.getMethod("getBody");
            check(getBody.getReturnType() == CompletionStage.class, "getBody returns CompletionStage");
            check(Modifier.isAbstract(getBody.getModifiers()), "getBody is abstract");
        } catch (NoSuchMethodException e) {
            getBody = null;
            check(false, "getBody missing: " + e);
        }
        // The interface adds exactly getBody() to Flow.Subscriber.
        check(HttpResponse.BodySubscriber.class.getDeclaredMethods().length == 1,
                "BodySubscriber declares exactly getBody()");

        // ---- deepened: Flow lifecycle to completion ----
        HttpResponse.BodySubscriber<String> full =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        full.onSubscribe(SUB);
        full.onNext(List.of(bb("sub")));
        full.onComplete();
        check("sub".equals(full.getBody().toCompletableFuture().get()),
                "subscriber assembles delivered bytes into the body");

        // getBody() returns the identical stage on every call (probed).
        CompletableFuture<String> f1 = full.getBody().toCompletableFuture();
        CompletableFuture<String> f2 = full.getBody().toCompletableFuture();
        check(f1 == f2, "getBody() is stable across calls");

        // Before any signals, the stage is incomplete.
        HttpResponse.BodySubscriber<String> pending =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        check(!pending.getBody().toCompletableFuture().isDone(),
                "getBody() incomplete before flow completion");

        // onError relays the exact cause.
        HttpResponse.BodySubscriber<String> failing =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        failing.onSubscribe(SUB);
        Exception boom = new Exception("boom");
        failing.onError(boom);
        try {
            failing.getBody().toCompletableFuture().get();
            check(false, "onError must fail the body");
        } catch (Exception e) {
            check(e.getCause() == boom, "onError propagates the exact cause object");
        }

        // onError before onSubscribe also fails the body.
        HttpResponse.BodySubscriber<String> noSub =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        noSub.onError(boom);
        check(noSub.getBody().toCompletableFuture().isCompletedExceptionally(),
                "onError without onSubscribe still fails the body");

        // onNext after onComplete is not tested (unspecified); multiple lists concatenated.
        HttpResponse.BodySubscriber<byte[]> multi =
                HttpResponse.BodySubscribers.ofByteArray();
        multi.onSubscribe(SUB);
        multi.onNext(List.of(bb("1"), bb("2")));
        multi.onNext(List.of(bb("3")));
        multi.onComplete();
        check(java.util.Arrays.equals(multi.getBody().toCompletableFuture().get(),
                new byte[]{'1', '2', '3'}), "multiple onNext lists concatenate in order");

        // Empty body: complete without onNext.
        HttpResponse.BodySubscriber<byte[]> empty =
                HttpResponse.BodySubscribers.ofByteArray();
        empty.onSubscribe(SUB);
        empty.onComplete();
        check(multi.getBody().toCompletableFuture().get().length == 3,
                "empty-body variant is independent");
        check(empty.getBody().toCompletableFuture().get().length == 0,
                "no onNext yields an empty byte array");

        // ---- deepened: user implementation is honored ----
        final AtomicInteger deliveries = new AtomicInteger();
        HttpResponse.BodySubscriber<String> custom = new HttpResponse.BodySubscriber<>() {
            final CompletableFuture<String> body = new CompletableFuture<>();
            public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
            public void onNext(List<ByteBuffer> items) { deliveries.incrementAndGet(); }
            public void onError(Throwable t) { body.completeExceptionally(t); }
            public void onComplete() { body.complete("custom-" + deliveries.get()); }
            public CompletionStage<String> getBody() { return body; }
        };
        custom.onSubscribe(SUB);
        custom.onNext(List.of(bb("a")));
        custom.onNext(List.of(bb("b")));
        custom.onComplete();
        check("custom-2".equals(custom.getBody().toCompletableFuture().get()),
                "user-implemented subscriber drives getBody()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
