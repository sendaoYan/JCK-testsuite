/*
 * @test
 * @summary java.net.http.HttpResponse.ResponseInfo conformance: interface accessors.
 * @run main ResponseInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ResponseInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.ResponseInfo.class.isInterface(), "ResponseInfo is an interface");
        check(HttpResponse.ResponseInfo.class.getTypeParameters().length == 0,
                "ResponseInfo is non-generic");

        HttpResponse.ResponseInfo info = (HttpResponse.ResponseInfo) Proxy.newProxyInstance(
                ResponseInfoTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.ResponseInfo.class},
                (p, m, a) -> {
                    switch (m.getName()) {
                        case "statusCode": return 201;
                        case "headers":
                            return HttpHeaders.of(Map.of("X", List.of("v")), (k, v) -> true);
                        case "version": return HttpClient.Version.HTTP_2;
                        default: return null;
                    }
                });

        check(info.statusCode() == 201, "statusCode");
        check("v".equals(info.headers().firstValue("X").orElse(null)), "headers");
        check(info.version() == HttpClient.Version.HTTP_2, "version");

        // ---- deepened: interface shape (all three methods abstract, no defaults) ----
        int abstractCount = 0, defaultCount = 0;
        for (Method m : HttpResponse.ResponseInfo.class.getMethods()) {
            if (m.getDeclaringClass() == HttpResponse.ResponseInfo.class) {
                if (m.isDefault()) defaultCount++;
                else abstractCount++;
            }
        }
        check(abstractCount == 3, "three abstract methods declared");
        check(defaultCount == 0, "no default methods on ResponseInfo");
        check(!HttpResponse.ResponseInfo.class.isSealed(), "ResponseInfo is user-implementable");
        try {
            check(Modifier.isAbstract(HttpResponse.ResponseInfo.class
                    .getMethod("statusCode").getModifiers()), "statusCode abstract");
            check(Modifier.isAbstract(HttpResponse.ResponseInfo.class
                    .getMethod("headers").getModifiers()), "headers abstract");
            check(Modifier.isAbstract(HttpResponse.ResponseInfo.class
                    .getMethod("version").getModifiers()), "version abstract");
            check(HttpResponse.ResponseInfo.class.getMethod("statusCode").getReturnType() == int.class,
                    "statusCode returns int");
            check(HttpResponse.ResponseInfo.class.getMethod("headers").getReturnType() == HttpHeaders.class,
                    "headers returns HttpHeaders");
            check(HttpResponse.ResponseInfo.class.getMethod("version").getReturnType() == HttpClient.Version.class,
                    "version returns Version");
        } catch (NoSuchMethodException e) {
            check(false, "unexpected: " + e);
        }

        // ---- deepened: anonymous implementations return their sentinels ----
        final HttpHeaders sentHeaders = HttpHeaders.of(
                Map.of("H", List.of("a", "b")), (k, v) -> true);
        HttpResponse.ResponseInfo anon = new HttpResponse.ResponseInfo() {
            public int statusCode() { return 599; }
            public HttpHeaders headers() { return sentHeaders; }
            public HttpClient.Version version() { return HttpClient.Version.HTTP_3; }
        };
        check(anon.statusCode() == 599, "anon statusCode sentinel");
        check(anon.headers() == sentHeaders, "anon headers identity");
        check(anon.headers().allValues("h").equals(List.of("a", "b")),
                "anon headers case-insensitive lookup");
        check(anon.version() == HttpClient.Version.HTTP_3, "anon version HTTP_3");

        // ---- deepened: ResponseInfo drives BodyHandler branching ----
        HttpResponse.BodyHandler<String> branching = ri ->
                ri.statusCode() < 400
                        ? HttpResponse.BodySubscribers.ofString(java.nio.charset.StandardCharsets.UTF_8)
                        : HttpResponse.BodySubscribers.replacing("error-" + ri.statusCode());
        HttpResponse.BodySubscriber<String> ok =
                branching.apply(new HttpResponse.ResponseInfo() {
                    public int statusCode() { return 200; }
                    public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
                    public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
                });
        ok.onSubscribe(new java.util.concurrent.Flow.Subscription() {
            public void request(long n) { } public void cancel() { } });
        ok.onComplete();
        check("".equals(ok.getBody().toCompletableFuture().join()),
                "2xx branch decodes the (empty) body");

        HttpResponse.BodySubscriber<String> err =
                branching.apply(new HttpResponse.ResponseInfo() {
                    public int statusCode() { return 500; }
                    public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
                    public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
                });
        err.onSubscribe(new java.util.concurrent.Flow.Subscription() {
            public void request(long n) { } public void cancel() { } });
        err.onComplete();
        check("error-500".equals(err.getBody().toCompletableFuture().join()),
                "5xx branch uses the status-code-dependent handler");

        // firstValue returns the first of multiple values.
        check("a".equals(anon.headers().firstValue("H").orElse(null)),
                "firstValue returns the first of multiple values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
