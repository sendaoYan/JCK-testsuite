/*
 * @test
 * @summary java.net.http.HttpRequest.BodyPublisher conformance: interface extends Flow.Publisher.
 * @run main BodyPublisherTest
 */

import java.net.http.HttpRequest;
import java.util.concurrent.Flow;

public class BodyPublisherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpRequest.BodyPublisher.class.isInterface(), "BodyPublisher is an interface");
        check(Flow.Publisher.class.isAssignableFrom(HttpRequest.BodyPublisher.class),
                "BodyPublisher extends Flow.Publisher");

        HttpRequest.BodyPublisher p = HttpRequest.BodyPublishers.ofString("hello");
        check(p != null, "ofString non-null");
        check(p instanceof Flow.Publisher, "ofString is a Flow.Publisher");
        check(p.contentLength() == 5, "ofString('hello') contentLength 5");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
