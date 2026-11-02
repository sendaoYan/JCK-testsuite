/*
 * @test
 * @summary java.util.concurrent.Flow.Subscription conformance: interface and
 *          request() called on subscribe.
 * @run main SubscriptionTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

public class SubscriptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Flow.Subscription.class.isInterface(), "Subscription is an interface");

        SubmissionPublisher<Integer> pub = new SubmissionPublisher<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Flow.Subscriber<Integer> sub = new Flow.Subscriber<Integer>() {
            public void onSubscribe(Flow.Subscription s) {
                s.request(Long.MAX_VALUE);
                latch.countDown();
            }
            public void onNext(Integer i) { }
            public void onError(Throwable t) { }
            public void onComplete() { }
        };
        pub.subscribe(sub);
        check(latch.await(5, TimeUnit.SECONDS), "request() invoked on subscribe");
        pub.close();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
