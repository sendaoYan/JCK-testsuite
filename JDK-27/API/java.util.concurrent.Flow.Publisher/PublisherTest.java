/*
 * @test
 * @summary java.util.concurrent.Flow.Publisher conformance: interface and
 *          subscribe() against a SubmissionPublisher.
 * @run main PublisherTest
 */

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class PublisherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Flow.Publisher.class.isInterface(), "Publisher is an interface");

        Flow.Publisher<Integer> pub = new SubmissionPublisher<>();
        Flow.Subscriber<Integer> sub = new Flow.Subscriber<Integer>() {
            public void onSubscribe(Flow.Subscription s) { s.request(1); }
            public void onNext(Integer i) { }
            public void onError(Throwable t) { }
            public void onComplete() { }
        };
        pub.subscribe(sub);
        check(((SubmissionPublisher<Integer>) pub).getNumberOfSubscribers() == 1,
              "subscribe() registers a subscriber");
        ((SubmissionPublisher<Integer>) pub).close();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
