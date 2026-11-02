/*
 * @test
 * @summary java.net.http.HttpRequest conformance: abstract class with GET defaults and equality.
 * @run main HttpRequestTest
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpOption;
import java.net.http.HttpRequest;
import java.util.Optional;

public class HttpRequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Modifier.isAbstract(HttpRequest.class.getModifiers()), "HttpRequest is abstract");

        HttpRequest r = HttpRequest.newBuilder(URI.create("https://example.com")).build();
        check(r != null, "newBuilder(URI).build() non-null");
        check("GET".equals(r.method()), "default method is GET");
        check(URI.create("https://example.com").equals(r.uri()), "uri preserved");
        check(!r.bodyPublisher().isPresent(), "no body by default");
        check(!r.timeout().isPresent(), "no timeout by default");
        check(!r.version().isPresent(), "no version by default");
        check(!r.expectContinue(), "expectContinue false by default");
        check(r.headers().map().isEmpty(), "headers empty by default");
        check(Optional.empty().equals(r.getOption(HttpOption.H3_DISCOVERY)), "unset option is empty");

        HttpRequest set = HttpRequest.newBuilder(URI.create("https://example.com"))
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ANY).build();
        check(HttpOption.Http3DiscoveryMode.ANY
                == set.getOption(HttpOption.H3_DISCOVERY).orElse(null), "set option readable");

        HttpRequest identical = HttpRequest.newBuilder(URI.create("https://example.com")).build();
        check(r.equals(identical), "identical requests are equal");
        check(r.hashCode() == identical.hashCode(), "equal requests have equal hashCode");

        HttpRequest copy = HttpRequest.newBuilder(r, (k, v) -> true).build();
        check(r.equals(copy), "newBuilder(request, filter) copies the request");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
