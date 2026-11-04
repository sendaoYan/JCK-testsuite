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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
