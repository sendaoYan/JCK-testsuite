/*
 * @test
 * @summary java.net.http.HttpResponse conformance: white-box structure + black-box interface and BodyHandler/Subscriber.
 * @run main HttpResponseTest
 */

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Flow;
import javax.net.ssl.SSLSession;

public class HttpResponseTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Method method(String name) {
        for (Method m : HttpResponse.class.getMethods()) {
            if (m.getName().equals(name)) return m;
        }
        return null;
    }

    static void testWhiteBox() throws Exception {
        check(HttpResponse.class.isInterface(), "HttpResponse is an interface");
        check(method("statusCode") != null && method("statusCode").getReturnType() == int.class,
                "statusCode returns int");
        check(method("request") != null && method("request").getReturnType() == HttpRequest.class,
                "request returns HttpRequest");
        check(method("headers") != null && method("headers").getReturnType() == HttpHeaders.class,
                "headers returns HttpHeaders");
        check(method("body") != null && method("body").getReturnType() == Object.class,
                "body returns the type parameter (erased to Object)");
        check(method("uri") != null && method("uri").getReturnType() == URI.class,
                "uri returns URI");
        check(method("version") != null && method("version").getReturnType() == HttpClient.Version.class,
                "version returns HttpClient.Version");
        check(method("connectionLabel") != null && method("connectionLabel").isDefault(),
                "connectionLabel is a default method");
        check(method("previousResponse") != null, "previousResponse present");
        check(method("sslSession") != null, "sslSession present");
        check(HttpResponse.BodyHandler.class.isInterface(), "BodyHandler is an interface");
        check(HttpResponse.BodySubscriber.class.isInterface(), "BodySubscriber is an interface");
        check(HttpResponse.PushPromiseHandler.class.isInterface(), "PushPromiseHandler is an interface");
        check(HttpResponse.ResponseInfo.class.isInterface(), "ResponseInfo is an interface");
    }

    static void testBlackBox() throws Exception {
        // Full anonymous implementation exercises every interface method (incl. default connectionLabel).
        HttpResponse<Integer> resp = new HttpResponse<>() {
            public int statusCode() { return 200; }
            public HttpRequest request() { return null; }
            public Optional<HttpResponse<Integer>> previousResponse() { return Optional.empty(); }
            public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
            public Integer body() { return 42; }
            public Optional<SSLSession> sslSession() { return Optional.empty(); }
            public URI uri() { return URI.create("https://example.com"); }
            public HttpClient.Version version() { return HttpClient.Version.HTTP_2; }
        };
        check(resp.statusCode() == 200, "statusCode");
        check(resp.body() == 42, "body");
        check(resp.version() == HttpClient.Version.HTTP_2, "version");
        check(URI.create("https://example.com").equals(resp.uri()), "uri");
        check(resp.headers().map().isEmpty(), "headers");
        check(!resp.previousResponse().isPresent(), "previousResponse empty");
        check(!resp.sslSession().isPresent(), "sslSession empty");
        check(resp.connectionLabel().isEmpty(), "connectionLabel default empty");

        // ResponseInfo + Flow protocol drives a BodyHandler/BodySubscriber to completion.
        HttpResponse.ResponseInfo info = new HttpResponse.ResponseInfo() {
            public int statusCode() { return 200; }
            public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
            public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
        };
        Flow.Subscription sub = new Flow.Subscription() {
            public void request(long n) { }
            public void cancel() { }
        };

        HttpResponse.BodyHandler<String> replacing = HttpResponse.BodyHandlers.replacing("hello");
        HttpResponse.BodySubscriber<String> hsub = replacing.apply(info);
        hsub.onSubscribe(sub);
        hsub.onComplete();
        check("hello".equals(hsub.getBody().toCompletableFuture().get()), "BodyHandlers.replacing completes");

        HttpResponse.BodySubscriber<Void> disc = HttpResponse.BodyHandlers.discarding().apply(info);
        disc.onSubscribe(sub);
        disc.onNext(List.of(ByteBuffer.wrap(new byte[]{1})));
        disc.onComplete();
        check(disc.getBody().toCompletableFuture().get() == null, "BodyHandlers.discarding completes null");

        // Factory methods produce non-null handlers/subscribers.
        check(HttpResponse.BodyHandlers.ofString() != null, "BodyHandlers.ofString");
        check(HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8) != null,
                "BodyHandlers.ofString(Charset)");
        check(HttpResponse.BodyHandlers.ofByteArray() != null, "BodyHandlers.ofByteArray");
        check(HttpResponse.BodySubscribers.ofString(java.nio.charset.StandardCharsets.UTF_8) != null,
                "BodySubscribers.ofString(Charset)");

        // PushPromiseHandler factory.
        ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<String>>> pushes = new ConcurrentHashMap<>();
        HttpResponse.PushPromiseHandler<String> pph =
                HttpResponse.PushPromiseHandler.of(req -> HttpResponse.BodyHandlers.ofString(), pushes);
        check(pph != null, "PushPromiseHandler.of non-null");
    }

    // ---- deepened: full-shape anonymous implementations exercising every method ----
    static void testCustomImplementations() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(java.net.URI.create("https://example.com/a")).build();

        // A complete user implementation: every interface method returns sentinels,
        // including the JDK-27 default connectionLabel() which stays empty.
        HttpResponse<String> resp = new HttpResponse<>() {
            public int statusCode() { return 301; }
            public HttpRequest request() { return req; }
            public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
            public HttpHeaders headers() { return HttpHeaders.of(Map.of("X", List.of("v")), (k, v) -> true); }
            public String body() { return "BODY"; }
            public Optional<SSLSession> sslSession() { return Optional.empty(); }
            public java.net.URI uri() { return java.net.URI.create("https://example.com/a"); }
            public HttpClient.Version version() { return HttpClient.Version.HTTP_3; }
        };
        check(resp.statusCode() == 301, "custom statusCode");
        check(resp.request() == req, "custom request identity");
        check("BODY".equals(resp.body()), "custom body");
        check(resp.version() == HttpClient.Version.HTTP_3, "custom HTTP_3 version");
        check("v".equals(resp.headers().firstValue("x").orElse(null)), "custom headers");
        check(resp.previousResponse().isEmpty(), "custom previousResponse empty");
        check(resp.sslSession().isEmpty(), "custom sslSession empty");
        check(resp.connectionLabel().isEmpty(), "connectionLabel default returns Optional.empty()");

        // previousResponse can chain to an earlier response.
        HttpResponse<String> first = new HttpResponse<>() {
            public int statusCode() { return 302; }
            public HttpRequest request() { return req; }
            public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
            public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
            public String body() { return null; }
            public Optional<SSLSession> sslSession() { return Optional.empty(); }
            public java.net.URI uri() { return java.net.URI.create("https://example.com/"); }
            public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
        };
        HttpResponse<String> second = new HttpResponse<>() {
            public int statusCode() { return 200; }
            public HttpRequest request() { return req; }
            public Optional<HttpResponse<String>> previousResponse() { return Optional.of(first); }
            public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
            public String body() { return "final"; }
            public Optional<SSLSession> sslSession() { return Optional.empty(); }
            public java.net.URI uri() { return java.net.URI.create("https://example.com/final"); }
            public HttpClient.Version version() { return HttpClient.Version.HTTP_2; }
        };
        check(second.previousResponse().isPresent(), "previousResponse present");
        check(second.previousResponse().get().statusCode() == 302,
                "previousResponse exposes the redirected response");
        check(first.body() == null, "null body is a legal sentinel");

        // sslSession can report a session object.
        final SSLSession session = (SSLSession) java.lang.reflect.Proxy.newProxyInstance(
                HttpResponseTest.class.getClassLoader(),
                new Class<?>[]{javax.net.ssl.SSLSession.class},
                (p, m, a) -> null);
        HttpResponse<Void> withSsl = new HttpResponse<>() {
            public int statusCode() { return 200; }
            public HttpRequest request() { return req; }
            public Optional<HttpResponse<Void>> previousResponse() { return Optional.empty(); }
            public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
            public Void body() { return null; }
            public Optional<SSLSession> sslSession() { return Optional.of(session); }
            public java.net.URI uri() { return java.net.URI.create("https://example.com/"); }
            public HttpClient.Version version() { return HttpClient.Version.HTTP_2; }
        };
        check(withSsl.sslSession().get() == session, "sslSession identity round-trip");
    }

    // ---- deepened: default-method and structural details ----
    static void testStructure() throws Exception {
        // connectionLabel is the only default method on HttpResponse.
        int defaults = 0;
        for (Method m : HttpResponse.class.getMethods()) {
            if (m.isDefault()) defaults++;
        }
        check(defaults == 1, "exactly one default method (connectionLabel)");
        check(method("connectionLabel").getReturnType() == Optional.class,
                "connectionLabel returns Optional<String>");
        check(method("previousResponse").getReturnType() == Optional.class,
                "previousResponse returns Optional");
        check(method("sslSession").getReturnType() == Optional.class, "sslSession returns Optional");
        check(!HttpResponse.class.isSealed(), "HttpResponse is not sealed (user-implementable)");
        check(HttpResponse.class.getTypeParameters().length == 1,
                "HttpResponse has one type parameter <T>");

        // BodySubscriber contract: getBody() returns the same stage on repeat calls,
        // incomplete until the flow finishes (probed).
        HttpResponse.BodySubscriber<String> pending =
                HttpResponse.BodySubscribers.ofString(java.nio.charset.StandardCharsets.UTF_8);
        java.util.concurrent.CompletableFuture<String> stage1 = pending.getBody().toCompletableFuture();
        java.util.concurrent.CompletableFuture<String> stage2 = pending.getBody().toCompletableFuture();
        check(stage1 == stage2, "getBody() returns the same instance (probed)");
        check(!stage1.isDone(), "getBody() incomplete before flow completion");
        pending.onSubscribe(new Flow.Subscription() {
            public void request(long n) { }
            public void cancel() { }
        });
        pending.onNext(List.of(ByteBuffer.wrap("now".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        pending.onComplete();
        check("now".equals(stage1.get()), "stage completes with the delivered body");

        // PushPromiseHandler: factory-free handler usable directly.
        HttpResponse.PushPromiseHandler<String> direct = (init, push, acceptor) ->
                acceptor.apply(HttpResponse.BodyHandlers.replacing("pushed"));
        HttpRequest a = HttpRequest.newBuilder(java.net.URI.create("https://example.com")).build();
        HttpRequest b = HttpRequest.newBuilder(java.net.URI.create("https://example.com/p")).build();
        java.util.concurrent.CompletableFuture<HttpResponse<String>> pushed = new java.util.concurrent.CompletableFuture<>();
        // The acceptor's returned future is what the implementation associates with the push.
        direct.applyPushPromise(a, b, bh -> pushed);
        check(!pushed.isDone(), "push future completion is driven by the client (not tested further)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCustomImplementations();
        testStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
