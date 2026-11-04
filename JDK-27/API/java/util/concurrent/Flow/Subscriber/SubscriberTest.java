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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
