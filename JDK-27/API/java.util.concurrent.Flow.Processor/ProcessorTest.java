/*
 * @test
 * @summary java.util.concurrent.Flow.Processor conformance: interface hierarchy.
 * @run main ProcessorTest
 */

import java.util.concurrent.Flow;

public class ProcessorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Flow.Processor.class.isInterface(), "Processor is an interface");
        check(Flow.Subscriber.class.isAssignableFrom(Flow.Processor.class),
              "Processor extends Subscriber");
        check(Flow.Publisher.class.isAssignableFrom(Flow.Processor.class),
              "Processor extends Publisher");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
