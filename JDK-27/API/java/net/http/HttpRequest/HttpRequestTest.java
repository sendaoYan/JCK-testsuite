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

    // ---- deepened: builder negative matrix (JDK-27 probed messages) ----
    static void testNegatives() {
        URI uri = URI.create("https://example.com/");

        expect(NullPointerException.class, () -> HttpRequest.newBuilder(null),
                "newBuilder(null) NPE");
        expect(IllegalStateException.class, () -> HttpRequest.newBuilder().build(),
                "build() without uri throws IllegalStateException");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder().uri(null),
                "uri(null) NPE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("a/b")),
                "uri(relative) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("ftp://h/")),
                "uri(ftp) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("file:///x")),
                "uri(file) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("http:/x")),
                "uri(http without host) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("ws://h/")),
                "uri(ws) IAE: request URIs must be http/https");

        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).header(null, "v"),
                "header(null name) NPE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).header("X", null),
                "header(null value) NPE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("X Y", "v"),
                "header(name with space) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("", "v"),
                "header(empty name) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("X", "a\nb"),
                "header(value with LF) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("X", "a\rb"),
                "header(value with CR) IAE");
        // JDK-27 probed: these restricted headers are rejected by default.
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("Connection", "close"),
                "header(Connection) restricted");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("content-length", "5"),
                "header(content-length) restricted");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("Host", "example.com"),
                "header(Host) restricted");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("Upgrade", "websocket"),
                "header(Upgrade) restricted");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).header("Expect", "100-continue"),
                "header(Expect) restricted");
        // HTAB is legal inside a field value (RFC 7230).
        check(HttpRequest.newBuilder(uri).header("X", "a\tb").build() != null,
                "header(value with HTAB) accepted");

        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).headers("A"),
                "headers(odd count) IAE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).headers((String[]) null),
                "headers(null array) NPE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).headers("A", "1", null, "2"),
                "headers(null element) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder(uri).headers("X Y", "v"), "headers(bad name) IAE");

        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).timeout(null),
                "timeout(null) NPE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).timeout(Duration.ZERO),
                "timeout(ZERO) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(uri).timeout(Duration.ofMillis(-1)),
                "timeout(negative) IAE");
        check(HttpRequest.newBuilder(uri).timeout(Duration.ofNanos(1)).build() != null,
                "timeout(1ns) accepted");

        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).POST(null),
                "POST(null) NPE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).PUT(null),
                "PUT(null) NPE");
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(uri).method(null, HttpRequest.BodyPublishers.noBody()),
                "method(null) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder(uri).method("", HttpRequest.BodyPublishers.noBody()),
                "method(empty) IAE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder(uri).method("CONNECT", HttpRequest.BodyPublishers.noBody()),
                "method(CONNECT) IAE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder(uri).method("GE T", HttpRequest.BodyPublishers.noBody()),
                "method(token with space) IAE");
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(uri).method("GET", null),
                "method(GET, null body) NPE");
        // Lowercase and hyphenated custom tokens are accepted.
        check("get".equals(HttpRequest.newBuilder(uri).method("get", HttpRequest.BodyPublishers.noBody())
                .build().method()), "lowercase method accepted");
        check("CUSTOM-Verb".equals(HttpRequest.newBuilder(uri)
                .method("CUSTOM-Verb", HttpRequest.BodyPublishers.noBody()).build().method()),
                "custom token method accepted");

        expect(NullPointerException.class, () -> HttpRequest.newBuilder(uri).version(null),
                "version(null) NPE");
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(uri).setOption(null, HttpOption.Http3DiscoveryMode.ANY),
                "setOption(null) NPE");
        expect(IllegalArgumentException.class, () -> setBadOptionType(),
                "setOption(H3_DISCOVERY, wrong type) IAE (raw-typed)");
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(null, (k, v) -> true), "newBuilder(null, filter) NPE");
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(HttpRequest.newBuilder(uri).build(), null),
                "newBuilder(request, null) NPE");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void setBadOptionType() {
        HttpRequest.newBuilder(URI.create("https://example.com/"))
                .setOption((HttpOption) HttpOption.H3_DISCOVERY, (Object) "notAMode");
    }

    // ---- deepened: verb/body interaction and builder state ----
    static void testMethodBodyInteraction() {
        URI uri = URI.create("https://example.com/");

        // GET()/DELETE() reset the body publisher previously set.
        HttpRequest afterGet = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString("b")).GET().build();
        check("GET".equals(afterGet.method()) && !afterGet.bodyPublisher().isPresent(),
                "POST then GET clears method and body");
        HttpRequest afterDelete = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString("b")).DELETE().build();
        check("DELETE".equals(afterDelete.method()) && !afterDelete.bodyPublisher().isPresent(),
                "POST then DELETE clears body");
        HttpRequest afterHead = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString("b")).HEAD().build();
        check("HEAD".equals(afterHead.method()) && !afterHead.bodyPublisher().isPresent(),
                "POST then HEAD clears body");
        HttpRequest afterPost = HttpRequest.newBuilder(uri).GET()
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        check("POST".equals(afterPost.method()) && afterPost.bodyPublisher().isPresent(),
                "GET then POST sets body");
        HttpRequest afterCustom = HttpRequest.newBuilder(uri).GET()
                .method("PATCH", HttpRequest.BodyPublishers.ofString("x")).build();
        check("PATCH".equals(afterCustom.method()) && afterCustom.bodyPublisher().isPresent(),
                "GET then method(PATCH) sets body");

        // Building twice from the same builder gives equal requests.
        HttpRequest.Builder sb = HttpRequest.newBuilder(uri).header("X", "1");
        check(sb.build().equals(sb.build()), "same builder builds equal requests");

        // copy() takes a structural snapshot: later mutation of the original
        // builder does not leak into the copy.
        HttpRequest.Builder original = HttpRequest.newBuilder(uri).header("X", "1");
        HttpRequest.Builder copied = original.copy();
        original.header("X", "2").setHeader("Y", "z");
        check(copied.build().headers().allValues("X").equals(List.of("1")),
                "copy() is independent of later original mutation");
        check(!copied.build().headers().firstValue("Y").isPresent(),
                "copy() does not see headers added after the copy");
        check(original.build().headers().allValues("X").equals(List.of("1", "2")),
                "original still accumulates");
    }

    // ---- deepened: equals/hashCode sensitivity (probed) ----
    static void testEqualsSensitivity() {
        URI uri = URI.create("https://example.com/");
        HttpRequest base = HttpRequest.newBuilder(uri).build();

        // Values NOT part of equality (probed JDK-27 behavior).
        check(base.equals(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(1)).build()),
                "equals ignores timeout");
        check(base.equals(HttpRequest.newBuilder(uri).expectContinue(true).build()),
                "equals ignores expectContinue");
        check(base.equals(HttpRequest.newBuilder(uri).version(HttpClient.Version.HTTP_1_1).build()),
                "equals ignores version");
        check(base.equals(HttpRequest.newBuilder(uri)
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ANY).build()),
                "equals ignores options");

        // Values that DO take part in equality.
        check(!base.equals(HttpRequest.newBuilder(uri).header("X", "1").build()),
                "equals reflects headers");
        check(!base.equals(HttpRequest.newBuilder(URI.create("https://example.com/x")).build()),
                "equals reflects uri");
        check(!base.equals(HttpRequest.newBuilder(uri).DELETE().build()),
                "equals reflects method");
        check(!base.equals(HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString("x")).build()),
                "equals reflects body");
        check(!base.equals(null), "not equal to null");
        check(!base.equals("str"), "not equal to unrelated type");
        check(base.hashCode() == HttpRequest.newBuilder(uri).build().hashCode(),
                "equal requests hash equally");

        // Header order does not affect equality (multi-value list equality does).
        check(HttpRequest.newBuilder(uri).header("A", "1").header("B", "2").build()
                        .equals(HttpRequest.newBuilder(uri).header("B", "2").header("A", "1").build()),
                "header insertion order irrelevant to equals");
        check(!HttpRequest.newBuilder(uri).header("A", "1").header("A", "2").build()
                        .equals(HttpRequest.newBuilder(uri).header("A", "2").header("A", "1").build()),
                "multi-value order matters within a header");
    }

    // ---- deepened: request immutability after build ----
    static void testImmutability() {
        URI uri = URI.create("https://example.com/");
        HttpRequest r = HttpRequest.newBuilder(uri).header("X", "1").build();
        expect(UnsupportedOperationException.class,
                () -> r.headers().map().put("Y", List.of("2")), "headers().map() unmodifiable");
        expect(UnsupportedOperationException.class,
                () -> r.headers().map().remove("X"), "headers().map() remove unsupported");
        expect(UnsupportedOperationException.class,
                () -> r.headers().allValues("X").add("2"), "allValues list unmodifiable");
        expect(UnsupportedOperationException.class,
                () -> r.headers().map().get("x").add("2"), "per-key list unmodifiable");
        expect(UnsupportedOperationException.class,
                () -> r.headers().map().entrySet().remove(r.headers().map().entrySet().iterator().next()),
                "entrySet remove unsupported");
    }

    // ---- deepened: newBuilder(request, filter) semantics ----
    static void testCopyFilterSemantics() {
        URI uri = URI.create("https://example.com/");
        HttpRequest src = HttpRequest.newBuilder(uri)
                .header("X", "1").header("X", "2").header("Y", "3")
                .timeout(Duration.ofSeconds(5))
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ALT_SVC)
                .POST(HttpRequest.BodyPublishers.ofString("body"))
                .build();

        java.util.List<String> filterSees = new java.util.ArrayList<>();
        HttpRequest copy = HttpRequest.newBuilder(src, (k, v) -> {
            filterSees.add(k + "=" + v);
            return !"Y".equalsIgnoreCase(k);
        }).build();

        // The filter is invoked once per header VALUE (probed).
        check(filterSees.equals(List.of("X=1", "X=2", "Y=3")),
                "filter invoked per header value in order");
        check(copy.headers().allValues("X").equals(List.of("1", "2")), "copy keeps unfiltered multi-values");
        check(!copy.headers().firstValue("Y").isPresent(), "copy drops filtered header");
        // Non-header state carries over unchanged.
        check("POST".equals(copy.method()), "copy keeps method");
        check(src.uri().equals(copy.uri()), "copy keeps uri");
        check(Duration.ofSeconds(5).equals(copy.timeout().get()), "copy keeps timeout");
        check(copy.bodyPublisher().isPresent()
                        && copy.bodyPublisher().get().contentLength() == 4L,
                "copy keeps body publisher");
        check(copy.getOption(HttpOption.H3_DISCOVERY)
                        .equals(Optional.of(HttpOption.Http3DiscoveryMode.ALT_SVC)),
                "copy keeps options (probed)");
        // A copy with an accept-all filter equals the source.
        check(src.equals(HttpRequest.newBuilder(src, (k, v) -> true).build()),
                "accept-all copy equals source");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNegatives();
        testMethodBodyInteraction();
        testEqualsSensitivity();
        testImmutability();
        testCopyFilterSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
