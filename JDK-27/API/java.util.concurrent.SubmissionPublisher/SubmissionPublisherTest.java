/*
 * @test
 * @summary java.util.concurrent.SubmissionPublisher conformance: subscribe,
 *          submit, consume, close, and the introspection accessors.
 * @run main SubmissionPublisherTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

public class SubmissionPublisherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        SubmissionPublisher<Integer> pub = new SubmissionPublisher<>();
        check(pub.getExecutor() != null, "getExecutor() non-null");
        check(pub.getMaxBufferCapacity() > 0, "getMaxBufferCapacity() > 0");
        check(!pub.isClosed(), "!isClosed() initially");
        check(pub.getNumberOfSubscribers() == 0, "no subscribers initially");
        check(!pub.hasSubscribers(), "!hasSubscribers() initially");

        CountDownLatch latch = new CountDownLatch(1);
        Flow.Subscriber<Integer> sub = new Flow.Subscriber<Integer>() {
            public void onSubscribe(Flow.Subscription s) { s.request(10); }
            public void onNext(Integer i) { if (i == 42) latch.countDown(); }
            public void onError(Throwable t) { }
            public void onComplete() { }
        };
        pub.subscribe(sub);
        check(pub.getNumberOfSubscribers() == 1, "one subscriber after subscribe");
        check(pub.isSubscribed(sub), "isSubscribed()");
        check(pub.getSubscribers().size() == 1, "getSubscribers() size 1");
        pub.submit(42);
        check(latch.await(5, TimeUnit.SECONDS), "submit() delivered to subscriber");
        pub.close();
        check(pub.isClosed(), "isClosed() after close()");

        // consume() collects all submitted items
        SubmissionPublisher<Integer> pub2 = new SubmissionPublisher<>();
        List<Integer> collected = new ArrayList<>();
        pub2.consume(collected::add);
        pub2.submit(1);
        pub2.submit(2);
        pub2.close();
        pub2.consume(collected::add).join();
        check(collected.contains(1) && collected.contains(2), "consume() collects items");

        // closeExceptionally()
        SubmissionPublisher<Integer> pub3 = new SubmissionPublisher<>();
        pub3.closeExceptionally(new RuntimeException("closed"));
        check(pub3.isClosed(), "closeExceptionally() closes");
        check(pub3.getClosedException() != null, "getClosedException() non-null");

        check(pub.estimateMinimumDemand() >= 0, "estimateMinimumDemand() >= 0");
        check(pub.estimateMaximumLag() >= 0, "estimateMaximumLag() >= 0");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
