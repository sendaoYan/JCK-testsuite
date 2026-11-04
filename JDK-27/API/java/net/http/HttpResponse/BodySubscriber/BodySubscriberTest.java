/*
 * @test
 * @summary java.net.http.HttpResponse.BodySubscriber conformance: interface extends Flow.Subscriber.
 * @run main BodySubscriberTest
 */

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public class BodySubscriberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.BodySubscriber.class.isInterface(), "BodySubscriber is an interface");
        check(Flow.Subscriber.class.isAssignableFrom(HttpResponse.BodySubscriber.class),
                "BodySubscriber extends Flow.Subscriber");

        HttpResponse.BodySubscriber<String> s =
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        check(s != null, "ofString non-null");
        check(s instanceof Flow.Subscriber, "ofString is a Flow.Subscriber");
        check(s.getBody() != null, "getBody() non-null");
        check(s.getBody() instanceof CompletionStage, "getBody() returns a CompletionStage");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
