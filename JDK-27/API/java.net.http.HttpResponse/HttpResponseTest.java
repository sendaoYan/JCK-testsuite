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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
