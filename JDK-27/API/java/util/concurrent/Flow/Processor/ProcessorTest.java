/*
 * @test
 * @summary java.util.concurrent.Flow$Processor conformance: white-box structure + black-box behavior.
 * @run main ProcessorTest
 */

import java.util.concurrent.*;

public class ProcessorTest {
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
        Class<?> c = cls("java.util.concurrent.Flow$Processor");
        check(c.isInterface(), "Processor is an interface");
        check(Flow.Subscriber.class.isAssignableFrom(c), "Processor extends Subscriber");
        check(Flow.Publisher.class.isAssignableFrom(c), "Processor extends Publisher");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Flow.Processor<Integer, Integer> p = new Forwarder();
        SubmissionPublisher<Integer> upstream = new SubmissionPublisher<>();
        CountDownLatch latch = new CountDownLatch(1);
        final int[] got = new int[1];
        p.subscribe(new Flow.Subscriber<Integer>() {
            public void onSubscribe(Flow.Subscription s) { s.request(1); }
            public void onNext(Integer i) { got[0] = i; latch.countDown(); }
            public void onError(Throwable t) { }
            public void onComplete() { }
        });
        upstream.subscribe(p);
        upstream.submit(42);
        check(latch.await(5, TimeUnit.SECONDS), "item flows through processor");
        check(got[0] == 42, "processor forwarded the item");
        upstream.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static class Forwarder implements Flow.Processor<Integer, Integer> {
        final SubmissionPublisher<Integer> out = new SubmissionPublisher<>();
        public void subscribe(Flow.Subscriber<? super Integer> s) { out.subscribe(s); }
        public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
        public void onNext(Integer i) { out.submit(i); }
        public void onError(Throwable t) { out.closeExceptionally(t); }
        public void onComplete() { out.close(); }
    }}
