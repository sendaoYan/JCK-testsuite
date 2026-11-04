/*
 * @test
 * @summary java.util.concurrent.Flow$Subscriber conformance: white-box structure + black-box behavior.
 * @run main SubscriberTest
 */

import java.util.concurrent.*;

public class SubscriberTest {
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
        Class<?> c = cls("java.util.concurrent.Flow$Subscriber");
        check(c.isInterface(), "Subscriber is an interface");
        hasMethod(c, "onSubscribe", Flow.Subscription.class);
        hasMethod(c, "onNext", Object.class);
        hasMethod(c, "onError", Throwable.class);
        hasMethod(c, "onComplete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
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
    }

    // ---- Part 3: contract deep-dive (four-callback contract with a spec-compliant
    //      hand-written publisher: ordering, terminal uniqueness, error identity) ----
    static final class EventLog {
        final java.util.List<String> log = new java.util.concurrent.CopyOnWriteArrayList<>();
        void add(String s) { log.add(s); }
        int count(String s) {
            int n = 0;
            for (String e : log) if (e.equals(s)) n++;
            return n;
        }
    }

    static final class ContractPublisher implements Flow.Publisher<String> {
        final EventLog log;
        ContractPublisher(EventLog log) { this.log = log; }
        public void subscribe(Flow.Subscriber<? super String> s) {
            s.onSubscribe(new Flow.Subscription() {
                int sent;
                boolean terminal;
                public void request(long n) {
                    if (n <= 0) {
                        if (!terminal) { terminal = true; s.onError(new IllegalArgumentException("bad request " + n)); }
                        return;
                    }
                    while (n-- > 0 && sent < 2 && !terminal) { sent++; s.onNext("m" + sent); }
                    if (sent >= 2 && !terminal) { terminal = true; s.onComplete(); }
                }
                public void cancel() { terminal = true; }
            });
        }
    }

    static void testCallbackOrdering() {
        EventLog ev = new EventLog();
        Flow.Subscriber<String> sub = new Flow.Subscriber<String>() {
            Flow.Subscription sub;
            public void onSubscribe(Flow.Subscription s) { ev.add("onSubscribe"); this.sub = s; s.request(1); }
            public void onNext(String m) { ev.add("onNext:" + m); sub.request(1); }
            public void onError(Throwable t) { ev.add("onError:" + t.getClass().getSimpleName()); }
            public void onComplete() { ev.add("onComplete"); }
        };
        new ContractPublisher(ev).subscribe(sub);
        check(ev.log.size() == 4, "onSubscribe, 2x onNext, onComplete: " + ev.log);
        check("onSubscribe".equals(ev.log.get(0)), "onSubscribe precedes all other signals");
        check("onNext:m1".equals(ev.log.get(1)) && "onNext:m2".equals(ev.log.get(2)),
                "onNext events in order");
        check("onComplete".equals(ev.log.get(3)), "onComplete is terminal");
        check(ev.count("onComplete") == 1 && ev.count("onError:IllegalArgumentException") == 0,
                "exactly one terminal signal, no onError after onComplete");
    }

    static void testErrorPath() {
        // subscriber receives onError(IllegalArgumentException) for a non-positive request
        EventLog ev = new EventLog();
        final Flow.Subscription[] held = new Flow.Subscription[1];
        Flow.Subscriber<String> sub = new Flow.Subscriber<String>() {
            public void onSubscribe(Flow.Subscription s) { ev.add("onSubscribe"); held[0] = s; }
            public void onNext(String m) { ev.add("onNext:" + m); }
            public void onError(Throwable t) { ev.add("onError:" + t.getClass().getSimpleName()); }
            public void onComplete() { ev.add("onComplete"); }
        };
        new ContractPublisher(ev).subscribe(sub);
        held[0].request(0);
        check(ev.log.size() == 2, "only onSubscribe then onError: " + ev.log);
        check("onError:IllegalArgumentException".equals(ev.log.get(1)),
                "request(0) terminates with onError(IllegalArgumentException)");
        // after a terminal signal no further onNext is delivered
        held[0].request(5);
        check(ev.log.size() == 2, "no signals after onError");
    }

    static void testSubmissionPublisherDelivery() throws Exception {
        ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sub-exec"); t.setDaemon(true); return t;
        });
        try {
            SubmissionPublisher<Integer> pub = new SubmissionPublisher<>(ex, 8);
            final CountDownLatch complete = new CountDownLatch(1);
            final java.util.List<Object> events = new java.util.concurrent.CopyOnWriteArrayList<>();
            Flow.Subscriber<Integer> sub = new Flow.Subscriber<Integer>() {
                public void onSubscribe(Flow.Subscription s) { events.add("sub:" + (s != null)); s.request(Long.MAX_VALUE); }
                public void onNext(Integer i) { events.add("next:" + i); }
                public void onError(Throwable t) { events.add("error:" + t.getMessage()); }
                public void onComplete() { events.add("complete"); complete.countDown(); }
            };
            pub.subscribe(sub);
            pub.submit(1);
            pub.submit(2);
            pub.close();
            check(complete.await(5, TimeUnit.SECONDS), "onComplete delivered after close");
            check(events.size() == 4, "sub, next:1, next:2, complete: " + events);
            check("next:1".equals(events.get(1)) && "next:2".equals(events.get(2)),
                    "onNext events in submit order");

            // closeExceptionally -> onError with the identical exception object
            SubmissionPublisher<Integer> pub2 = new SubmissionPublisher<>(ex, 8);
            final CountDownLatch failed = new CountDownLatch(1);
            final Throwable[] gotErr = new Throwable[1];
            pub2.subscribe(new Flow.Subscriber<Integer>() {
                public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
                public void onNext(Integer i) { }
                public void onError(Throwable t) { gotErr[0] = t; failed.countDown(); }
                public void onComplete() { }
            });
            RuntimeException boom = new RuntimeException("subBoom");
            pub2.closeExceptionally(boom);
            check(failed.await(5, TimeUnit.SECONDS), "onError delivered after closeExceptionally");
            check(gotErr[0] == boom, "onError carries the identical exception object");
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCallbackOrdering();
        testErrorPath();
        testSubmissionPublisherDelivery();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
