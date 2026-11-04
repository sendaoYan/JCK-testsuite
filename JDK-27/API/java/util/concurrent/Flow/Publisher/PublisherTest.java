/*
 * @test
 * @summary java.util.concurrent.Flow$Publisher conformance: white-box structure + black-box behavior.
 * @run main PublisherTest
 */

import java.util.concurrent.*;

public class PublisherTest {
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
        Class<?> c = cls("java.util.concurrent.Flow$Publisher");
        check(c.isInterface(), "Publisher is an interface");
        hasMethod(c, "subscribe", Flow.Subscriber.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
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
    }

    // ---- Part 3: contract deep-dive ----
    // a fully synchronous Publisher: subscribe() drives the whole contract inline
    static final class RangePublisher implements Flow.Publisher<Integer> {
        final int lo, hi;
        RangePublisher(int lo, int hi) { this.lo = lo; this.hi = hi; }
        public void subscribe(Flow.Subscriber<? super Integer> s) {
            Flow.Subscription sub = new Flow.Subscription() {
                long demand;
                int cur = lo;
                boolean done;
                public void request(long n) {
                    if (n <= 0) { done = true; s.onError(new IllegalArgumentException("non-positive request")); return; }
                    demand += n;
                    while (cur <= hi && demand > 0 && !done) { s.onNext(cur++); demand--; }
                    if (cur > hi && !done) { done = true; s.onComplete(); }
                }
                public void cancel() { done = true; }
            };
            s.onSubscribe(sub);
        }
    }

    static final class Collector implements Flow.Subscriber<Integer> {
        final java.util.List<Integer> received = new java.util.concurrent.CopyOnWriteArrayList<>();
        final java.util.List<String> events = new java.util.concurrent.CopyOnWriteArrayList<>();
        volatile Flow.Subscription sub;
        final long initialRequest;
        Collector(long initialRequest) { this.initialRequest = initialRequest; }
        public void onSubscribe(Flow.Subscription s) { sub = s; events.add("subscribe"); if (initialRequest > 0) s.request(initialRequest); }
        public void onNext(Integer i) { received.add(i); events.add("next:" + i); }
        public void onError(Throwable t) { events.add("error"); }
        public void onComplete() { events.add("complete"); }
    }

    static void testHandWrittenPublisherContract() throws Exception {
        // one subscriber receives the full range in order
        Collector c = new Collector(4);
        new RangePublisher(1, 3).subscribe(c);
        check(c.events.size() == 5, "onSubscribe + 3x onNext + onComplete: " + c.events);
        check(c.received.equals(java.util.Arrays.asList(1, 2, 3)), "items in publisher order: " + c.received);
        check("complete".equals(c.events.get(c.events.size() - 1)), "terminal onComplete");

        // demand gating: request(1) delivers only one item
        Collector slow = new Collector(1);
        new RangePublisher(1, 5).subscribe(slow);
        check(slow.received.equals(java.util.Arrays.asList(1)), "request(1) delivers exactly one item");
        slow.sub.request(2);
        check(slow.received.equals(java.util.Arrays.asList(1, 2, 3)), "further requests resume delivery: " + slow.received);
        slow.sub.cancel();
        slow.sub.request(10);
        check(slow.received.equals(java.util.Arrays.asList(1, 2, 3)), "cancel stops all further onNext");

        // two independent subscribers each get the full sequence
        Collector a = new Collector(Long.MAX_VALUE);
        Collector b = new Collector(Long.MAX_VALUE);
        RangePublisher rp = new RangePublisher(1, 2);
        rp.subscribe(a);
        rp.subscribe(b);
        check(a.received.equals(java.util.Arrays.asList(1, 2)) && b.received.equals(java.util.Arrays.asList(1, 2)),
                "both subscribers receive everything");
    }

    static void testSubmissionPublisherAsPublisher() throws Exception {
        ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "pub-exec"); t.setDaemon(true); return t;
        });
        try {
            // interface-typed use: publish through Flow.Publisher only
            Flow.Publisher<String> pub = new SubmissionPublisher<>(ex, 8);
            final CountDownLatch got = new CountDownLatch(1);
            final java.util.List<String> box = new java.util.concurrent.CopyOnWriteArrayList<>();
            pub.subscribe(new Flow.Subscriber<String>() {
                public void onSubscribe(Flow.Subscription s) { s.request(3); }
                public void onNext(String i) { box.add(i); got.countDown(); }
                public void onError(Throwable t) { }
                public void onComplete() { }
            });
            ((SubmissionPublisher<String>) pub).submit("hello");
            check(got.await(5, TimeUnit.SECONDS), "item delivered via interface-typed publisher");
            check(java.util.Arrays.asList("hello").equals(box), "delivered item exact: " + box);
            ((SubmissionPublisher<String>) pub).close();
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHandWrittenPublisherContract();
        testSubmissionPublisherAsPublisher();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
