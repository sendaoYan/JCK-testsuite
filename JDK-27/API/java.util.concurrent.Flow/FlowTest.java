/*
 * @test
 * @summary java.util.concurrent.Flow conformance: final class and defaultBufferSize.
 * @run main FlowTest
 */

import java.util.concurrent.Flow;

public class FlowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(!Flow.class.isInterface(), "Flow is a final class");
        check(Flow.defaultBufferSize() > 0, "defaultBufferSize() > 0");

        // nested reactive-streams types are interfaces
        check(Flow.Publisher.class.isInterface(), "Publisher is an interface");
        check(Flow.Subscriber.class.isInterface(), "Subscriber is an interface");
        check(Flow.Subscription.class.isInterface(), "Subscription is an interface");
        check(Flow.Processor.class.isInterface(), "Processor is an interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
