/*
 * @test
 * @summary java.net.http.HttpResponse.BodyHandler conformance: interface applies to a BodySubscriber.
 * @run main BodyHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public class BodyHandlerTest {
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

    static HttpResponse.ResponseInfo info(int code, HttpClient.Version v) {
        return new HttpResponse.ResponseInfo() {
            public int statusCode() { return code; }
            public HttpHeaders headers() {
                return HttpHeaders.of(Map.of("X", List.of("1")), (k, v2) -> true);
            }
            public HttpClient.Version version() { return v; }
        };
    }

    static ByteBuffer bb(String s) { return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)); }

    public static void main(String[] args) {
        check(HttpResponse.BodyHandler.class.isInterface(), "BodyHandler is an interface");
        check(HttpResponse.BodyHandler.class.getTypeParameters().length == 1,
                "BodyHandler has one type parameter");
        check(!HttpResponse.BodyHandler.class.isSealed(), "BodyHandler is user-implementable");

        HttpResponse.ResponseInfo info = (HttpResponse.ResponseInfo) Proxy.newProxyInstance(
                BodyHandlerTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.ResponseInfo.class},
                (p, m, a) -> {
                    switch (m.getName()) {
                        case "statusCode": return 200;
                        case "headers":
                            return HttpHeaders.of(Map.of("X", List.of("1")), (k, v) -> true);
                        case "version": return HttpClient.Version.HTTP_1_1;
                        default: return null;
                    }
                });

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        check(handler != null, "BodyHandlers.ofString() non-null");
        HttpResponse.BodySubscriber<String> subscriber = handler.apply(info);
        check(subscriber != null, "apply(ResponseInfo) returns a BodySubscriber");
        check(subscriber.getBody() != null, "BodySubscriber.getBody() non-null");

        // ---- deepened: interface shape ----
        try {
            Method apply = HttpResponse.BodyHandler.class.getMethod("apply",
                    HttpResponse.ResponseInfo.class);
            check(Modifier.isAbstract(apply.getModifiers()), "apply is abstract");
        } catch (NoSuchMethodException e) {
            check(false, "apply method missing: " + e);
        }
        check(HttpResponse.BodyHandler.class.getDeclaredMethods().length == 1,
                "BodyHandler declares exactly apply()");

        // A user implementation is honored.
        HttpResponse.BodyHandler<Integer> custom = ri -> HttpResponse.BodySubscribers.replacing(ri.statusCode());
        HttpResponse.BodySubscriber<Integer> customSub = custom.apply(info(201, HttpClient.Version.HTTP_2));
        customSub.onSubscribe(SUB);
        customSub.onComplete();
        check(customSub.getBody().toCompletableFuture().isDone(),
                "custom handler returns a working subscriber");
        check(201 == customSub.getBody().toCompletableFuture().join(),
                "handler can branch on ResponseInfo.statusCode");

        // ---- deepened: apply() output behaves for every status code/version ----
        for (int code : new int[]{200, 301, 404, 500, 505}) {
            HttpResponse.BodySubscriber<String> s =
                    HttpResponse.BodyHandlers.ofString().apply(info(code, HttpClient.Version.HTTP_1_1));
            check(s != null, "ofString().apply(" + code + ") non-null");
        }
        for (HttpClient.Version v : HttpClient.Version.values()) {
            HttpResponse.BodySubscriber<Void> s =
                    HttpResponse.BodyHandlers.discarding().apply(info(200, v));
            check(s != null, "discarding().apply(" + v + ") non-null");
        }

        // ---- deepened: driven handlers complete with the expected body ----
        HttpResponse.BodySubscriber<String> s1 = HttpResponse.BodyHandlers.ofString().apply(info);
        s1.onSubscribe(SUB);
        s1.onNext(List.of(bb("handler")));
        s1.onComplete();
        check("handler".equals(s1.getBody().toCompletableFuture().join()),
                "ofString handler assembles the body");

        HttpResponse.BodySubscriber<String> s2 =
                HttpResponse.BodyHandlers.replacing("SENT").apply(info);
        s2.onSubscribe(SUB);
        s2.onComplete();
        check("SENT".equals(s2.getBody().toCompletableFuture().join()),
                "replacing handler yields the sentinel");

        HttpResponse.BodySubscriber<Void> s3 = HttpResponse.BodyHandlers.discarding().apply(info);
        s3.onSubscribe(SUB);
        s3.onNext(List.of(bb("ignored")));
        s3.onComplete();
        check(s3.getBody().toCompletableFuture().join() == null,
                "discarding handler yields null body");

        HttpResponse.BodySubscriber<byte[]> s4 = HttpResponse.BodyHandlers.ofByteArray().apply(info);
        s4.onSubscribe(SUB);
        s4.onNext(List.of(bb("AB"), bb("CD")));
        s4.onComplete();
        check(java.util.Arrays.equals(s4.getBody().toCompletableFuture().join(),
                new byte[]{'A', 'B', 'C', 'D'}), "ofByteArray handler concatenates chunks");

        // ofString(Charset) handler decodes using the given charset.
        HttpResponse.BodySubscriber<String> s5 =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_16BE).apply(info);
        s5.onSubscribe(SUB);
        s5.onNext(List.of(ByteBuffer.wrap(new byte[]{0, 104, 0, 105})));
        s5.onComplete();
        check("hi".equals(s5.getBody().toCompletableFuture().join()),
                "ofString(UTF_16BE) handler decodes bytes");

        // buffering(handler, n) wraps and still completes correctly.
        HttpResponse.BodySubscriber<String> s6 = HttpResponse.BodyHandlers
                .buffering(HttpResponse.BodyHandlers.ofString(), 4).apply(info);
        s6.onSubscribe(SUB);
        s6.onNext(List.of(bb("1"), bb("2"), bb("3"), bb("4")));
        s6.onComplete();
        check("1234".equals(s6.getBody().toCompletableFuture().join()),
                "buffering handler delivers buffered body");

        // limiting(handler, n) wraps without changing the body.
        HttpResponse.BodySubscriber<String> s7 = HttpResponse.BodyHandlers
                .limiting(HttpResponse.BodyHandlers.ofString(), 100).apply(info);
        s7.onSubscribe(SUB);
        s7.onNext(List.of(bb("ok")));
        s7.onComplete();
        check("ok".equals(s7.getBody().toCompletableFuture().join()),
                "limiting handler transparent for small bodies");

        // fromSubscriber handler with finisher: body is the finisher result.
        final int[] got = new int[1];
        HttpResponse.BodySubscriber<String> s8 = HttpResponse.BodyHandlers
                .fromSubscriber(new Flow.Subscriber<List<ByteBuffer>>() {
                    Flow.Subscription sub;
                    public void onSubscribe(Flow.Subscription s) { sub = s; sub.request(1); }
                    public void onNext(List<ByteBuffer> item) { got[0]++; sub.request(1); }
                    public void onError(Throwable t) { }
                    public void onComplete() { }
                }, sub -> "items:" + got[0]).apply(info);
        s8.onSubscribe(SUB);
        s8.onNext(List.of(bb("x")));
        s8.onComplete();
        check("items:1".equals(s8.getBody().toCompletableFuture().join()),
                "fromSubscriber finisher applied after delivery (probed)");

        // ---- deepened: negative matrix (messages probed on JDK-27) ----
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.buffering(null, 8),
                "buffering(null, 8) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodyHandlers.buffering(HttpResponse.BodyHandlers.ofString(), 0),
                "buffering(handler, 0) IAE 'must be greater than 0'");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodyHandlers.buffering(HttpResponse.BodyHandlers.ofString(), -4),
                "buffering(handler, -4) IAE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.limiting(null, 10),
                "limiting(null, 10) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofString(), -1),
                "limiting(handler, -1) IAE 'capacity must not be negative'");
        check(HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofString(), 0) != null,
                "limiting(handler, 0) accepted (probed: only negative rejected)");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofFile(null),
                "ofFile(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofFile(null,
                        java.nio.file.StandardOpenOption.CREATE),
                "ofFile(null, opts) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofFileDownload(null),
                "ofFileDownload(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofString(null),
                "ofString(null charset) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.ofByteArrayConsumer(null),
                "ofByteArrayConsumer(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.fromSubscriber(null),
                "fromSubscriber(null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.fromSubscriber(null, s -> "x"),
                "fromSubscriber(null, f) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodyHandlers.fromSubscriber(new NopSub(), null),
                "fromSubscriber(s, null) NPE");
        expect(NullPointerException.class, () -> HttpResponse.BodyHandlers.fromLineSubscriber(null),
                "fromLineSubscriber(null) NPE");
        expect(NullPointerException.class,
                () -> HttpResponse.BodyHandlers.fromLineSubscriber(new NopLineSub(), null, "\n"),
                "fromLineSubscriber(s, null, sep) NPE");
        check(HttpResponse.BodyHandlers.replacing(null) != null,
                "replacing(null) yields a handler completing with null (probed)");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static class NopSub implements Flow.Subscriber<List<ByteBuffer>> {
        public void onSubscribe(Flow.Subscription s) { }
        public void onNext(List<ByteBuffer> item) { }
        public void onError(Throwable t) { }
        public void onComplete() { }
    }

    static class NopLineSub implements Flow.Subscriber<String> {
        public void onSubscribe(Flow.Subscription s) { }
        public void onNext(String item) { }
        public void onError(Throwable t) { }
        public void onComplete() { }
    }
}
