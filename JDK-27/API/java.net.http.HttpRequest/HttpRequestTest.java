/*
 * @test
 * @summary java.net.http.HttpRequest conformance: white-box structure + black-box builder/body-publisher behavior.
 * @run main HttpRequestTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpOption;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class HttpRequestTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpRequest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == Object.class, "superclass is Object");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor != null, "no-arg constructor present");
        check(Modifier.isProtected(ctor.getModifiers()), "constructor is protected");

        for (String m : new String[]{"method", "uri", "headers", "bodyPublisher", "timeout", "expectContinue",
                "version", "getOption", "equals", "hashCode"}) {
            check(hasMethod(c, m), "HttpRequest has method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    static void testBlackBox() {
        URI uri = URI.create("https://example.com");

        HttpRequest r = HttpRequest.newBuilder(uri).build();
        check(r != null, "newBuilder(URI).build() non-null");
        check("GET".equals(r.method()), "default method is GET");
        check(uri.equals(r.uri()), "uri preserved");
        check(!r.bodyPublisher().isPresent(), "no body by default");
        check(!r.timeout().isPresent(), "no timeout by default");
        check(!r.version().isPresent(), "no version by default");
        check(!r.expectContinue(), "expectContinue false by default");
        check(r.headers().map().isEmpty(), "headers empty by default");
        check(Optional.empty().equals(r.getOption(HttpOption.H3_DISCOVERY)), "unset option is empty");

        // Method selection helpers.
        check("GET".equals(HttpRequest.newBuilder(uri).GET().build().method()), "GET() sets GET");
        check("DELETE".equals(HttpRequest.newBuilder(uri).DELETE().build().method()), "DELETE() sets DELETE");
        check("HEAD".equals(HttpRequest.newBuilder(uri).HEAD().build().method()), "HEAD() sets HEAD");
        check("POST".equals(HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString("body")).build().method()), "POST() sets POST");
        check("PUT".equals(HttpRequest.newBuilder(uri)
                .PUT(HttpRequest.BodyPublishers.noBody()).build().method()), "PUT() sets PUT");
        check("PATCH".equals(HttpRequest.newBuilder(uri)
                .method("PATCH", HttpRequest.BodyPublishers.noBody()).build().method()), "method() custom verb");

        // Body publisher: content length and presence.
        HttpRequest withBody = HttpRequest.newBuilder(uri)
                .method("POST", HttpRequest.BodyPublishers.ofString("body")).build();
        check(withBody.bodyPublisher().isPresent(), "bodyPublisher present for POST");
        check(withBody.bodyPublisher().get().contentLength() == 4L, "ofString body contentLength 4");

        HttpRequest.BodyPublisher noBody = HttpRequest.BodyPublishers.noBody();
        check(noBody.contentLength() == 0L, "noBody contentLength 0");
        check(HttpRequest.BodyPublishers.ofString("abc").contentLength() == 3L, "ofString contentLength 3");
        check(HttpRequest.BodyPublishers.ofString("abc",
                java.nio.charset.StandardCharsets.UTF_8).contentLength() == 3L, "ofString(Charset) contentLength 3");
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3, 4, 5}).contentLength() == 5L,
                "ofByteArray contentLength 5");
        check(HttpRequest.BodyPublishers.ofByteArray(new byte[]{1, 2, 3}, 1, 2).contentLength() == 2L,
                "ofByteArray(offset,len) contentLength 2");

        // Headers: header() accumulates, setHeader() overwrites, headers() adds pairs.
        HttpRequest hdr = HttpRequest.newBuilder(uri)
                .header("X-A", "1")
                .header("X-A", "2")
                .setHeader("X-B", "3")
                .headers("X-C", "4", "X-D", "5")
                .build();
        check(hdr.headers().allValues("X-A").equals(List.of("1", "2")), "header() accumulates");
        check(hdr.headers().allValues("X-B").equals(List.of("3")), "setHeader() value");
        check(hdr.headers().allValues("X-C").equals(List.of("4")), "headers() pair C");
        check(hdr.headers().allValues("X-D").equals(List.of("5")), "headers() pair D");

        HttpRequest replaced = HttpRequest.newBuilder(uri)
                .header("X-A", "1").setHeader("X-A", "9").build();
        check(replaced.headers().allValues("X-A").equals(List.of("9")), "setHeader() overwrites");

        // Timeout/version/expectContinue options.
        HttpRequest options = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(9))
                .version(HttpClient.Version.HTTP_1_1)
                .expectContinue(true)
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ANY)
                .build();
        check(Duration.ofSeconds(9).equals(options.timeout().get()), "timeout read back");
        check(options.version().get() == HttpClient.Version.HTTP_1_1, "version read back");
        check(options.expectContinue(), "expectContinue true read back");
        check(HttpOption.Http3DiscoveryMode.ANY == options.getOption(HttpOption.H3_DISCOVERY).orElse(null),
                "set option readable");

        // no-arg newBuilder() + uri().
        HttpRequest fromNoArg = HttpRequest.newBuilder().uri(uri).build();
        check(uri.equals(fromNoArg.uri()), "no-arg newBuilder().uri()");

        // Equality and copy.
        HttpRequest identical = HttpRequest.newBuilder(uri).build();
        check(r.equals(identical), "identical requests are equal");
        check(r.hashCode() == identical.hashCode(), "equal requests have equal hashCode");

        HttpRequest copy = HttpRequest.newBuilder(r, (k, v) -> true).build();
        check(r.equals(copy), "newBuilder(request, filter) copies the request");

        // Filtered copy: drop a header.
        HttpRequest withHeader = HttpRequest.newBuilder(uri).header("X-Drop", "yes").build();
        HttpRequest filtered = HttpRequest.newBuilder(withHeader, (k, v) -> !"X-Drop".equalsIgnoreCase(k)).build();
        check(!filtered.headers().firstValue("X-Drop").isPresent(), "filtered copy drops header");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
