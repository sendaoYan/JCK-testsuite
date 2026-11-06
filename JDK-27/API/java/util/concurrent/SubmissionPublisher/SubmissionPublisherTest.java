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

    // ---- Part 3: contract deep-dive ----
    static ExecutorService namedPool() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sp-exec"); t.setDaemon(true); return t;
        });
    }

    static final class GreedySub implements Flow.Subscriber<Integer> {
        final List<Integer> items = new CopyOnWriteArrayList<>();
        volatile String threadName;
        volatile boolean completed;
        volatile Throwable error;
        final CountDownLatch firstItem = new CountDownLatch(1);
        final CountDownLatch terminal = new CountDownLatch(1);
        public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
        public void onNext(Integer i) { items.add(i); threadName = Thread.currentThread().getName(); firstItem.countDown(); }
        public void onError(Throwable t) { error = t; terminal.countDown(); }
        public void onComplete() { completed = true; terminal.countDown(); }
    }

    static final class PassiveSub implements Flow.Subscriber<Integer> {
        volatile Throwable error;
        final CountDownLatch terminal = new CountDownLatch(1);
        public void onSubscribe(Flow.Subscription s) { } // never requests
        public void onNext(Integer i) { }
        public void onError(Throwable t) { error = t; terminal.countDown(); }
        public void onComplete() { terminal.countDown(); }
    }

    static void testConstructorsAndDefaults() {
        SubmissionPublisher<Integer> def = new SubmissionPublisher<>();
        check(def.getMaxBufferCapacity() == Flow.defaultBufferSize(),
                "default maxBufferCapacity == Flow.defaultBufferSize() == " + Flow.defaultBufferSize());
        check(def.getExecutor() != null, "default executor allocated");
        check(!def.hasSubscribers() && def.getNumberOfSubscribers() == 0, "no subscribers initially");
        check(!def.isClosed() && def.getClosedException() == null, "open, no close exception");
        def.close();

        ExecutorService ex = namedPool();
        try {
            SubmissionPublisher<String> custom = new SubmissionPublisher<>(ex, 7);
            check(custom.getExecutor() == ex, "getExecutor returns the supplied executor");
            // capacity is rounded up to a power of two: 7 -> 8, exact powers pass through
            check(custom.getMaxBufferCapacity() == 8, "capacity 7 rounds up to 8: " + custom.getMaxBufferCapacity());
            custom.close();
            SubmissionPublisher<String> exact = new SubmissionPublisher<>(ex, 8);
            check(exact.getMaxBufferCapacity() == 8, "power-of-two capacity 8 is preserved");
            exact.close();
        } finally {
            ex.shutdown();
        }
    }

    static void testSubmitLagAndDelivery() throws Exception {
        ExecutorService ex = namedPool();
        try {
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 8);
            GreedySub sub = new GreedySub();
            pub.subscribe(sub);
            int lag1 = pub.submit(7);
            check(lag1 >= 0 && lag1 <= 1, "submit returns estimated lag (0 or 1): " + lag1);
            check(sub.firstItem.await(5, TimeUnit.SECONDS), "item delivered");
            check(sub.items.equals(Collections.singletonList(7)), "delivered exactly the item: " + sub.items);
            check(sub.threadName != null && sub.threadName.equals("sp-exec"),
                    "subscriber callbacks run on the publisher's executor thread: " + sub.threadName);
            int lag2 = pub.submit(8);
            check(lag2 >= 0, "second submit returns non-negative lag: " + lag2);
            check(pub.estimateMaximumLag() >= 0, "estimateMaximumLag non-negative");

            // close -> onComplete for each subscriber; close is idempotent
            pub.close();
            check(sub.terminal.await(5, TimeUnit.SECONDS), "terminal signal after close");
            check(sub.completed && sub.error == null, "onComplete delivered, no error");
            check(pub.isClosed(), "isClosed");
            pub.close(); // second close must not throw
            check(true, "close is idempotent (second close returns normally)");
            try {
                pub.submit(9);
                check(false, "submit after close should throw");
            } catch (IllegalStateException expected) {
                check("Closed".equals(expected.getMessage()), "submit-after-close message 'Closed'");
            }
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    static void testCloseExceptionallyContract() throws Exception {
        ExecutorService ex = namedPool();
        try {
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 8);
            GreedySub sub = new GreedySub();
            pub.subscribe(sub);
            RuntimeException boom = new RuntimeException("ceBoom");
            pub.closeExceptionally(boom);
            check(pub.isClosed(), "closed after closeExceptionally");
            check(pub.getClosedException() == boom, "getClosedException returns the identical object");
            check(sub.terminal.await(5, TimeUnit.SECONDS), "terminal signal after closeExceptionally");
            check(sub.error == boom, "onError carries the identical exception: " + sub.error);
            check(!sub.completed, "no onComplete on the error path");

            // a normal close first wins: later closeExceptionally does not replace it
            SubmissionPublisher<Integer> pub2 = new SubmissionPublisher<>(ex, 8);
            GreedySub sub2 = new GreedySub();
            pub2.subscribe(sub2);
            pub2.close();
            pub2.closeExceptionally(new RuntimeException("late"));
            check(pub2.getClosedException() == null, "closeExceptionally after close has no effect");
            check(sub2.terminal.await(5, TimeUnit.SECONDS), "sub2 terminal");
            check(sub2.completed, "subscriber completed normally (first close wins)");
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    static void testSubscriptionBookkeeping() throws Exception {
        ExecutorService ex = namedPool();
        try {
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 8);
            PassiveSub a = new PassiveSub();
            PassiveSub b = new PassiveSub();
            pub.subscribe(a);
            pub.subscribe(b);
            check(pub.getNumberOfSubscribers() == 2 && pub.hasSubscribers(), "two subscribers");
            check(pub.isSubscribed(a) && pub.isSubscribed(b), "both registered");
            // re-subscribing the same subscriber: JDK-27 (unlike JDK 9-17) delivers
            // onError(IllegalStateException) to the existing subscription (documented
            // in the SubmissionPublisher javadoc) and then closes/removes it — it is
            // NOT a silent no-op. Verify the signal, not a frozen count.
            pub.subscribe(a);
            check(a.terminal.await(5, TimeUnit.SECONDS),
                    "re-subscribe delivers a terminal signal to the existing subscription");
            check(a.error instanceof IllegalStateException,
                    "re-subscribe onError is IllegalStateException: " + a.error);
            check(pub.isSubscribed(b), "the other subscriber remains subscribed");
            // the duplicate subscription is closed asynchronously; poll until the
            // count settles back to only `b`, proving no third entry was added.
            int n;
            int spins = 0;
            while ((n = pub.getNumberOfSubscribers()) > 1 && spins++ < 500) {
                Thread.sleep(10);
            }
            check(n == 1, "re-subscribe removed the duplicate subscription (settled to 1, not " + n + ")");
            // subscribe(null) -> NPE
            try {
                pub.subscribe(null);
                check(false, "subscribe(null) should throw NPE");
            } catch (NullPointerException expected) {
                check(true, "subscribe(null) throws NullPointerException");
            }
            // getSubscribers returns an independent snapshot
            List<Flow.Subscriber<? super Integer>> snapshot = pub.getSubscribers();
            int before = pub.getNumberOfSubscribers();
            snapshot.clear();
            check(pub.getNumberOfSubscribers() == before, "snapshot mutation does not affect the publisher");
            check(snapshot.size() != pub.getNumberOfSubscribers(), "snapshot was a mutable copy");
            pub.close();
            pub.close(); // idempotent
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    static void testOfferWithFullBuffer() throws Exception {
        ExecutorService ex = namedPool();
        try {
            // buffer capacity 2, subscriber never requests: items fill then overflow
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 2);
            pub.subscribe(new PassiveSub());
            int r1 = pub.offer(1, 50, TimeUnit.MILLISECONDS, null);
            check(r1 >= 0, "offer into empty buffer returns estimated lag: " + r1);
            int r2 = pub.offer(2, 50, TimeUnit.MILLISECONDS, null);
            check(r2 >= 0, "second offer still buffered: " + r2);
            int r3 = pub.offer(3, 50, TimeUnit.MILLISECONDS, null);
            check(r3 < 0, "offer with full buffer and no demand returns negative (dropped): " + r3);
            // with a retry handler returning true the item is dropped from that subscriber too
            int r4 = pub.offer(4, 50, TimeUnit.MILLISECONDS, (s, i) -> true);
            check(r4 < 0, "offer with drop handler on full buffer still reports drop: " + r4);
            pub.close();

            // offer with an eager subscriber succeeds
            SubmissionPublisher<Integer> pub2 = new SubmissionPublisher<>(ex, 4);
            GreedySub sub = new GreedySub();
            pub2.subscribe(sub);
            int ok = pub2.offer(9, 100, TimeUnit.MILLISECONDS, null);
            check(ok >= 0, "offer with demand succeeds: " + ok);
            check(sub.firstItem.await(5, TimeUnit.SECONDS), "offered item delivered");
            pub2.close();
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    static void testSubscriberFailureUnsubscribes() throws Exception {
        ExecutorService ex = namedPool();
        try {
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 8);
            final CountDownLatch errored = new CountDownLatch(1);
            final Throwable[] seen = new Throwable[1];
            Flow.Subscriber<Integer> boomer = new Flow.Subscriber<Integer>() {
                public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
                public void onNext(Integer i) { throw new IllegalStateException("onNextBoom"); }
                public void onError(Throwable t) { seen[0] = t; errored.countDown(); }
                public void onComplete() { }
            };
            pub.subscribe(boomer);
            check(pub.isSubscribed(boomer), "registered before failure");
            pub.submit(1);
            check(errored.await(5, TimeUnit.SECONDS), "failing subscriber got onError");
            check(seen[0] instanceof IllegalStateException
                    && "onNextBoom".equals(seen[0].getMessage()),
                    "onError receives the exception thrown by onNext: " + seen[0]);
            check(!pub.isSubscribed(boomer), "failing subscriber was unsubscribed");
            check(pub.getNumberOfSubscribers() == 0, "publisher has no subscribers left");
            pub.close();
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    static void testConsumeCompletes() throws Exception {
        ExecutorService ex = namedPool();
        try {
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 8);
            List<Integer> got = new ArrayList<>();
            CompletableFuture<Void> cf = pub.consume(got::add);
            check(!cf.isDone(), "consume future incomplete while publisher open");
            pub.submit(1);
            pub.submit(2);
            pub.submit(3);
            pub.close();
            cf.get(5, TimeUnit.SECONDS);
            check(got.equals(Arrays.asList(1, 2, 3)), "consume sees items in order: " + got);
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorsAndDefaults();
        testSubmitLagAndDelivery();
        testCloseExceptionallyContract();
        testSubscriptionBookkeeping();
        testOfferWithFullBuffer();
        testSubscriberFailureUnsubscribes();
        testConsumeCompletes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
