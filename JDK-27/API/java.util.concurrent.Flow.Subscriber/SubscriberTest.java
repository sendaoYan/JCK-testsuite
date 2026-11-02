/*
 * @test
 * @summary java.util.concurrent.Flow.Subscriber conformance: interface and
 *          onSubscribe/onNext delivery via SubmissionPublisher.
 * @run main SubscriberTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

public class SubscriberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Flow.Subscriber.class.isInterface(), "Subscriber is an interface");

        SubmissionPublisher<Integer> pub = new SubmissionPublisher<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] got = new int[1];
        Flow.Subscriber<Integer> sub = new Flow.Subscriber<Integer>() {
            public void onSubscribe(Flow.Subscription s) { s.request(1); }
            public void onNext(Integer i) { got[0] = i; latch.countDown(); }
            public void onError(Throwable t) { }
            public void onComplete() { }
        };
        pub.subscribe(sub);
        pub.submit(42);
        check(latch.await(5, TimeUnit.SECONDS), "onNext delivered within timeout");
        check(got[0] == 42, "onNext received the submitted item");
        pub.close();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
