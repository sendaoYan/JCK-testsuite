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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
