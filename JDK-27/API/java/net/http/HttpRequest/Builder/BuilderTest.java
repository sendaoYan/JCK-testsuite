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

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
