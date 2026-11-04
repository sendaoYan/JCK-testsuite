/*
 * @test
 * @summary java.util.concurrent.Flow$Subscription conformance: white-box structure + black-box behavior.
 * @run main SubscriptionTest
 */

import java.util.concurrent.*;

public class SubscriptionTest {
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
        Class<?> c = cls("java.util.concurrent.Flow$Subscription");
        check(c.isInterface(), "Subscription is an interface");
        hasMethod(c, "request", long.class);
        hasMethod(c, "cancel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SubmissionPublisher<Integer> pub = new SubmissionPublisher<>();
        final CountDownLatch requested = new CountDownLatch(1);
        Flow.Subscriber<Integer> sub = new Flow.Subscriber<Integer>() {
            public void onSubscribe(Flow.Subscription s) {
                s.request(Long.MAX_VALUE);
                requested.countDown();
            }
            public void onNext(Integer i) { }
            public void onError(Throwable t) { }
            public void onComplete() { }
        };
        pub.subscribe(sub);
        check(requested.await(5, TimeUnit.SECONDS), "request() invoked on subscribe");
        pub.close();
    }

    // ---- Part 3: contract deep-dive (cumulative demand semantics, request validation,
    //      cancel semantics - driven through a spec-implementing hand-written subscription) ----
    static final class DemandSubscriptionPublisher implements Flow.Publisher<Integer> {
        final java.util.List<Integer> items;
        final java.util.List<String> trace = new java.util.concurrent.CopyOnWriteArrayList<>();
        DemandSubscriptionPublisher(java.util.List<Integer> items) { this.items = items; }
        public void subscribe(Flow.Subscriber<? super Integer> s) {
            trace.add("subscribe");
            s.onSubscribe(new Flow.Subscription() {
                long pending;
                int cursor;
                boolean cancelled;
                public void request(long n) {
                    trace.add("request:" + n);
                    if (n <= 0) {
                        trace.add("onError");
                        s.onError(new IllegalArgumentException("3.9"));
                        return;
                    }
                    pending = pending + n;
                    if (pending < 0) pending = Long.MAX_VALUE; // saturation per rule 3.17
                    while (cursor < items.size() && pending > 0 && !cancelled) {
                        s.onNext(items.get(cursor++));
                        pending--;
                    }
                    if (cursor == items.size() && pending >= 0 && !cancelled
                            && !trace.contains("onComplete")) {
                        trace.add("onComplete");
                        s.onComplete();
                    }
                }
                public void cancel() { trace.add("cancel"); cancelled = true; }
            });
        }
    }

    static final class TrackingSubscriber implements Flow.Subscriber<Integer> {
        final java.util.List<Integer> received = new java.util.concurrent.CopyOnWriteArrayList<>();
        final java.util.List<String> signals = new java.util.concurrent.CopyOnWriteArrayList<>();
        volatile Flow.Subscription sub;
        public void onSubscribe(Flow.Subscription s) { sub = s; signals.add("onSubscribe"); }
        public void onNext(Integer i) { received.add(i); }
        public void onError(Throwable t) { signals.add("onError:" + t.getClass().getSimpleName()); }
        public void onComplete() { signals.add("onComplete"); }
    }

    static void testCumulativeDemand() {
        DemandSubscriptionPublisher pub =
                new DemandSubscriptionPublisher(java.util.Arrays.asList(1, 2, 3, 4, 5));
        TrackingSubscriber sub = new TrackingSubscriber();
        pub.subscribe(sub);
        check(pub.trace.get(0).equals("subscribe"), "subscribe is the publisher's entry point");
        sub.sub.request(2);
        check(sub.received.equals(java.util.Arrays.asList(1, 2)),
                "request(2) delivers exactly two items: " + sub.received);
        sub.sub.request(1);
        check(sub.received.equals(java.util.Arrays.asList(1, 2, 3)),
                "demand accumulates across requests: " + sub.received);
        sub.sub.request(Long.MAX_VALUE);
        check(sub.received.equals(java.util.Arrays.asList(1, 2, 3, 4, 5)),
                "final request drains the remainder: " + sub.received);
        check(sub.signals.contains("onComplete"), "onComplete after the stream drains");
    }

    static void testRequestValidation() {
        DemandSubscriptionPublisher pub = new DemandSubscriptionPublisher(java.util.Arrays.asList(1));
        TrackingSubscriber sub = new TrackingSubscriber();
        pub.subscribe(sub);
        sub.sub.request(0);
        check(sub.signals.contains("onError:IllegalArgumentException"),
                "request(0) -> onError(IllegalArgumentException): " + sub.signals);
        DemandSubscriptionPublisher pub2 = new DemandSubscriptionPublisher(java.util.Arrays.asList(1));
        TrackingSubscriber sub2 = new TrackingSubscriber();
        pub2.subscribe(sub2);
        sub2.sub.request(-1);
        check(sub2.signals.contains("onError:IllegalArgumentException"),
                "request(-1) -> onError(IllegalArgumentException): " + sub2.signals);
        check(sub2.received.isEmpty(), "no item delivered around a non-positive request");
    }

    static void testCancelSemantics() {
        DemandSubscriptionPublisher pub = new DemandSubscriptionPublisher(java.util.Arrays.asList(1, 2, 3));
        TrackingSubscriber sub = new TrackingSubscriber();
        pub.subscribe(sub);
        sub.sub.request(1);
        check(sub.received.equals(java.util.Arrays.asList(1)), "one item before cancel");
        sub.sub.cancel();
        sub.sub.request(10);
        check(sub.received.equals(java.util.Arrays.asList(1)),
                "after cancel, further requests deliver nothing: " + sub.received);
        sub.sub.cancel(); // idempotent by convention: no error, no signal
        check(!sub.signals.contains("onComplete"), "cancelled subscription never completes");
    }

    static void testSubmissionPublisherSubscription() throws Exception {
        ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "subs-exec"); t.setDaemon(true); return t;
        });
        try {
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 8);
            final java.util.List<Integer> received = new java.util.concurrent.CopyOnWriteArrayList<>();
            final CountDownLatch twoDelivered = new CountDownLatch(2);
            final CountDownLatch threeDelivered = new CountDownLatch(3);
            final CountDownLatch terminal = new CountDownLatch(1);
            final Flow.Subscription[] held = new Flow.Subscription[1];
            pub.subscribe(new Flow.Subscriber<Integer>() {
                public void onSubscribe(Flow.Subscription s) { held[0] = s; }
                public void onNext(Integer i) { received.add(i); twoDelivered.countDown(); threeDelivered.countDown(); }
                public void onError(Throwable t) { terminal.countDown(); }
                public void onComplete() { terminal.countDown(); }
            });
            pub.submit(1);
            pub.submit(2);
            pub.submit(3);
            // demand gating is causal, not timing-based: nothing can be delivered
            // until this subscriber calls request(), and it never did
            check(received.isEmpty(), "no delivery before request (demand gating)");
            held[0].request(2);
            check(twoDelivered.await(5, TimeUnit.SECONDS), "two items delivered after request(2)");
            check(received.equals(java.util.Arrays.asList(1, 2)), "FIFO order, only the two oldest: " + received);
            held[0].request(1);
            check(threeDelivered.await(5, TimeUnit.SECONDS), "third item delivered after request(1)");
            check(received.equals(java.util.Arrays.asList(1, 2, 3)), "remaining item after final request");
            // cancel stops delivery of subsequently submitted items; a cancelled
            // subscription receives no further signals at all (including onComplete)
            pub.submit(4);
            held[0].cancel();
            pub.close();
            check(!received.contains(4), "no onNext after cancel");
            check(terminal.getCount() == 1, "cancelled subscription gets no terminal signal");
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCumulativeDemand();
        testRequestValidation();
        testCancelSemantics();
        testSubmissionPublisherSubscription();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
