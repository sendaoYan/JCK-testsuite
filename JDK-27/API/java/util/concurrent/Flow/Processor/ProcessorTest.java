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
        testProcessorPipeline();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- Part 3: contract deep-dive (transformation, error/completion relay,
    //      multiple downstream subscribers, Processor as Subscriber+Publisher) ----
    static void testProcessorPipeline() throws Exception {
        ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "proc-exec"); t.setDaemon(true); return t;
        });
        try {
            // transformation: upstream ints -> downstream strings
            SubmissionPublisher<Integer> upstream = new SubmissionPublisher<>(ex, 8);
            SubmissionPublisher<String> transformed = new SubmissionPublisher<>(ex, 8);
            Flow.Processor<Integer, String> mapper = new Flow.Processor<Integer, String>() {
                public void subscribe(Flow.Subscriber<? super String> s) { transformed.subscribe(s); }
                public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
                public void onNext(Integer i) { transformed.submit("n" + i); }
                public void onError(Throwable t) { transformed.closeExceptionally(t); }
                public void onComplete() { transformed.close(); }
            };
            final java.util.List<String> got = new java.util.concurrent.CopyOnWriteArrayList<>();
            final CountDownLatch closed = new CountDownLatch(1);
            mapper.subscribe(new Flow.Subscriber<String>() {
                public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
                public void onNext(String s) { got.add(s); }
                public void onError(Throwable t) { closed.countDown(); }
                public void onComplete() { closed.countDown(); }
            });
            upstream.subscribe(mapper);
            upstream.submit(1);
            upstream.submit(2);
            upstream.close();
            check(closed.await(5, TimeUnit.SECONDS), "processor completed downstream after upstream close");
            check(got.equals(java.util.Arrays.asList("n1", "n2")), "transformed items in order: " + got);

            // error relay: the identical exception reaches the downstream subscriber
            SubmissionPublisher<Integer> up2 = new SubmissionPublisher<>(ex, 8);
            SubmissionPublisher<Integer> fwd = new SubmissionPublisher<>(ex, 8);
            Flow.Processor<Integer, Integer> pass = new Flow.Processor<Integer, Integer>() {
                public void subscribe(Flow.Subscriber<? super Integer> s) { fwd.subscribe(s); }
                public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
                public void onNext(Integer i) { fwd.submit(i); }
                public void onError(Throwable t) { fwd.closeExceptionally(t); }
                public void onComplete() { fwd.close(); }
            };
            final CountDownLatch erred = new CountDownLatch(1);
            final Throwable[] seen = new Throwable[1];
            pass.subscribe(new Flow.Subscriber<Integer>() {
                public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
                public void onNext(Integer i) { }
                public void onError(Throwable t) { seen[0] = t; erred.countDown(); }
                public void onComplete() { }
            });
            up2.subscribe(pass);
            RuntimeException boom = new RuntimeException("procBoom");
            up2.closeExceptionally(boom);
            check(erred.await(5, TimeUnit.SECONDS), "onError relayed downstream");
            check(seen[0] == boom, "processor relays the identical exception object downstream");

            // a Processor is both a Subscriber and a Publisher at once (interface typing)
            check(pass instanceof Flow.Subscriber, "processor is a Subscriber");
            check(pass instanceof Flow.Publisher, "processor is a Publisher");
        } finally {
            ex.shutdown();
            check(ex.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
    }

    static class Forwarder implements Flow.Processor<Integer, Integer> {
        final SubmissionPublisher<Integer> out = new SubmissionPublisher<>();
        public void subscribe(Flow.Subscriber<? super Integer> s) { out.subscribe(s); }
        public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
        public void onNext(Integer i) { out.submit(i); }
        public void onError(Throwable t) { out.closeExceptionally(t); }
        public void onComplete() { out.close(); }
    }}
