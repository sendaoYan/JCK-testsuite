/*
 * @test
 * @summary java.net.http.HttpRequest.Builder conformance: method verbs, headers, options and copy.
 * @run main BuilderTest
 */

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpOption;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;

public class BuilderTest {
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

    public static void main(String[] args) {
        check(HttpRequest.Builder.class.isInterface(), "Builder is an interface");

        HttpRequest.Builder b = HttpRequest.newBuilder();
        check(b != null, "newBuilder() non-null");
        check(b.uri(URI.create("https://example.com")) == b, "uri returns this");
        check(b.header("X", "y") == b, "header returns this");
        check(b.GET() == b, "GET returns this");

        HttpRequest r = HttpRequest.newBuilder(URI.create("https://example.com"))
                .GET()
                .header("X-One", "1")
                .headers("A", "1", "B", "2")
                .build();
        check("GET".equals(r.method()), "GET verb");
        check("https://example.com".equals(r.uri().toString()), "uri");
        check("1".equals(r.headers().firstValue("X-One").orElse(null)), "single header");
        check("1".equals(r.headers().firstValue("A").orElse(null)), "headers A");
        check("2".equals(r.headers().firstValue("B").orElse(null)), "headers B");

        HttpRequest post = HttpRequest.newBuilder(URI.create("https://example.com"))
                .POST(HttpRequest.BodyPublishers.ofString("payload")).build();
        check("POST".equals(post.method()), "POST verb");
        check(post.bodyPublisher().isPresent(), "POST body present");

        HttpRequest put = HttpRequest.newBuilder(URI.create("https://example.com"))
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        check("PUT".equals(put.method()), "PUT verb");

        HttpRequest del = HttpRequest.newBuilder(URI.create("https://example.com")).DELETE().build();
        check("DELETE".equals(del.method()), "DELETE verb");

        HttpRequest head = HttpRequest.newBuilder(URI.create("https://example.com")).HEAD().build();
        check("HEAD".equals(head.method()), "HEAD verb");

        HttpRequest custom = HttpRequest.newBuilder(URI.create("https://example.com"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("x")).build();
        check("PATCH".equals(custom.method()), "custom method verb");

        HttpRequest setHeader = HttpRequest.newBuilder(URI.create("https://example.com"))
                .header("X", "a").setHeader("X", "b").build();
        check("b".equals(setHeader.headers().firstValue("X").orElse(null)), "setHeader replaces");

        HttpRequest timed = HttpRequest.newBuilder(URI.create("https://example.com"))
                .timeout(Duration.ofSeconds(3))
                .expectContinue(true)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        check(Duration.ofSeconds(3).equals(timed.timeout().orElse(null)), "timeout persisted");
        check(timed.expectContinue(), "expectContinue persisted");
        check(HttpClient.Version.HTTP_1_1 == timed.version().orElse(null), "version persisted");

        HttpRequest withOption = HttpRequest.newBuilder(URI.create("https://example.com"))
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ALT_SVC).build();
        check(HttpOption.Http3DiscoveryMode.ALT_SVC
                == withOption.getOption(HttpOption.H3_DISCOVERY).orElse(null), "setOption persisted");

        HttpRequest.Builder original = HttpRequest.newBuilder(URI.create("https://example.com")).GET();
        HttpRequest.Builder copied = original.copy();
        check(original.build().equals(copied.build()), "copy() produces equal request");

        // ---- deepened: "returns this" for every setter ----
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create("https://example.com"));
        check(rb.setHeader("A", "1") == rb, "setHeader returns this");
        check(rb.headers("X", "y") == rb, "headers returns this");
        check(rb.POST(HttpRequest.BodyPublishers.noBody()) == rb, "POST returns this");
        check(rb.PUT(HttpRequest.BodyPublishers.noBody()) == rb, "PUT returns this");
        check(rb.DELETE() == rb, "DELETE returns this");
        check(rb.HEAD() == rb, "HEAD returns this");
        check(rb.method("PATCH", HttpRequest.BodyPublishers.noBody()) == rb, "method returns this");
        check(rb.expectContinue(false) == rb, "expectContinue returns this");
        check(rb.timeout(Duration.ofSeconds(1)) == rb, "timeout returns this");
        check(rb.version(HttpClient.Version.HTTP_2) == rb, "version returns this");
        check(rb.setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ANY) == rb,
                "setOption returns this");
        check(rb.copy() != rb, "copy returns a new builder");

        // ---- deepened: negative matrix (messages probed on JDK-27) ----
        URI u = URI.create("https://example.com");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder().uri(null), "uri(null) NPE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("a/b")),
                "uri(relative) IAE: undefined scheme");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("ftp://h/")),
                "uri(ftp) IAE: invalid scheme");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder().uri(URI.create("http:/x")),
                "uri(no host) IAE: unsupported URI");
        expect(IllegalStateException.class, () -> HttpRequest.newBuilder().build(),
                "build() without uri ISE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(u).header(null, "v"),
                "header(null, v) NPE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(u).header("X", null),
                "header(X, null) NPE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(u).header("X Y", "v"),
                "header(bad name) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(u).header("X", "v\n"),
                "header(LF value) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(u).setHeader("X", "v\r"),
                "setHeader(CR value) IAE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(u).headers("a", "1", "b"),
                "headers(odd) IAE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(u).headers((String[]) null),
                "headers(null) NPE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(u).timeout(null),
                "timeout(null) NPE");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(u).timeout(Duration.ZERO),
                "timeout(ZERO) IAE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder(u).timeout(Duration.ofSeconds(-1)), "timeout(negative) IAE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(u).version(null),
                "version(null) NPE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(u).POST(null), "POST(null) NPE");
        expect(NullPointerException.class, () -> HttpRequest.newBuilder(u).PUT(null), "PUT(null) NPE");
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(u).method(null, HttpRequest.BodyPublishers.noBody()),
                "method(null,...) NPE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder(u).method("", HttpRequest.BodyPublishers.noBody()),
                "method(empty,...) IAE");
        expect(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder(u).method("CONNECT", HttpRequest.BodyPublishers.noBody()),
                "method(CONNECT,...) IAE");
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(u).setOption(null, HttpOption.Http3DiscoveryMode.ANY),
                "setOption(null,...) NPE");

        // setOption with a wrong value type is an IAE (needs raw types to express).
        expect(IllegalArgumentException.class, () -> setBadOptionType(),
                "setOption(H3_DISCOVERY, String) IAE");

        // ---- deepened: default state of a fresh builder-built request ----
        HttpRequest fresh = HttpRequest.newBuilder(u).build();
        check("GET".equals(fresh.method()), "default verb GET");
        check(!fresh.bodyPublisher().isPresent(), "default body absent");
        check(!fresh.timeout().isPresent(), "default timeout absent");
        check(!fresh.version().isPresent(), "default version absent");
        check(!fresh.expectContinue(), "default expectContinue false");
        check(fresh.headers().map().isEmpty(), "default headers empty");
        check(!fresh.getOption(HttpOption.H3_DISCOVERY).isPresent(), "default option absent");

        // ---- deepened: header accumulation and ordering ----
        HttpRequest multi = HttpRequest.newBuilder(u)
                .header("M", "1").header("M", "2").setHeader("N", "x")
                .setHeader("N", "y").build();
        check(multi.headers().allValues("m").equals(List.of("1", "2")),
                "header() accumulates in call order");
        check(multi.headers().allValues("n").equals(List.of("y")),
                "setHeader() replaces previous values");
        check(multi.headers().firstValue("M").orElse(null).equals("1"),
                "firstValue returns first added");
        check(multi.headers().map().keySet().equals(java.util.Set.of("M", "N")),
                "map keys preserve original spelling");

        // Empty header value is legal; empty header name is not.
        check("".equals(HttpRequest.newBuilder(u).header("E", "").build()
                .headers().firstValue("E").orElse(null)), "empty value accepted");
        expect(IllegalArgumentException.class, () -> HttpRequest.newBuilder(u).header("", "v"),
                "empty name rejected");

        // ---- deepened: copy() independence ----
        HttpRequest.Builder one = HttpRequest.newBuilder(u)
                .header("H", "1").timeout(Duration.ofSeconds(2)).GET();
        HttpRequest.Builder snap = one.copy();
        one.header("H", "2").DELETE();
        HttpRequest snapReq = snap.build();
        check(snapReq.headers().allValues("H").equals(List.of("1")),
                "copy unaffected by original mutation (header)");
        check("GET".equals(snapReq.method()), "copy unaffected by original mutation (method)");
        check(Duration.ofSeconds(2).equals(snapReq.timeout().orElse(null)),
                "copy carries timeout snapshot");
        // A copied builder can itself be copied again.
        check(snap.copy().build().equals(snapReq), "copy of copy equals copy");

        // ---- deepened: option lifecycle through the builder ----
        HttpRequest opt = HttpRequest.newBuilder(u)
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY)
                .setOption(HttpOption.H3_DISCOVERY, null)
                .build();
        check(!opt.getOption(HttpOption.H3_DISCOVERY).isPresent(),
                "setOption(option, null) removes the option (probed)");
        HttpRequest everyMode = HttpRequest.newBuilder(u)
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ANY).build();
        check(HttpOption.Http3DiscoveryMode.ANY == everyMode.getOption(HttpOption.H3_DISCOVERY).orElse(null),
                "ANY round-trips");

        System.out.println("PASS (" + passed + " assertions)");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void setBadOptionType() {
        HttpRequest.newBuilder(URI.create("https://example.com"))
                .setOption((HttpOption) HttpOption.H3_DISCOVERY, (Object) "wrong");
    }
}
