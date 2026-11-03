/*
 * @test
 * @summary java.util.concurrent.SubmissionPublisher conformance: white-box structure + black-box behavior.
 * @run main SubmissionPublisherTest
 */

import java.util.*;
import java.util.concurrent.*;

public class SubmissionPublisherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.SubmissionPublisher");
        check(!c.isInterface(), "class");
        check(Flow.Publisher.class.isAssignableFrom(c), "implements Flow.Publisher");
        check(AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");
        hasMethod(c, "subscribe", Flow.Subscriber.class);
        hasMethod(c, "submit", Object.class);
        hasMethod(c, "offer", Object.class, java.util.function.BiPredicate.class);
        hasMethod(c, "close");
        hasMethod(c, "closeExceptionally", Throwable.class);
        hasMethod(c, "isClosed");
        hasMethod(c, "getSubscribers");
        hasMethod(c, "getNumberOfSubscribers");
        hasMethod(c, "hasSubscribers");
        hasMethod(c, "consume", java.util.function.Consumer.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SubmissionPublisher<Integer> pub = new SubmissionPublisher<>();
        check(pub.getExecutor() != null, "getExecutor() non-null");
        check(pub.getMaxBufferCapacity() > 0, "getMaxBufferCapacity() > 0");
        check(!pub.isClosed() && pub.getNumberOfSubscribers() == 0, "initial state");

        CountDownLatch latch = new CountDownLatch(1);
        Flow.Subscriber<Integer> sub = new Flow.Subscriber<Integer>() {
            public void onSubscribe(Flow.Subscription s) { s.request(10); }
            public void onNext(Integer i) { if (i == 42) latch.countDown(); }
            public void onError(Throwable t) { }
            public void onComplete() { }
        };
        pub.subscribe(sub);
        check(pub.getNumberOfSubscribers() == 1 && pub.isSubscribed(sub), "subscribe registers");
        check(pub.getSubscribers().size() == 1, "getSubscribers size 1");
        pub.submit(42);
        check(latch.await(5, TimeUnit.SECONDS), "submit delivered");
        pub.close();
        check(pub.isClosed(), "isClosed after close");

        SubmissionPublisher<Integer> pub2 = new SubmissionPublisher<>();
        List<Integer> collected = new ArrayList<>();
        pub2.consume(collected::add);
        pub2.submit(1);
        pub2.submit(2);
        pub2.close();
        pub2.consume(collected::add).join();
        check(collected.contains(1) && collected.contains(2), "consume collects items");

        SubmissionPublisher<Integer> pub3 = new SubmissionPublisher<>();
        pub3.closeExceptionally(new RuntimeException("closed"));
        check(pub3.isClosed() && pub3.getClosedException() != null, "closeExceptionally");
        check(pub.estimateMinimumDemand() >= 0 && pub.estimateMaximumLag() >= 0, "estimates non-negative");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
