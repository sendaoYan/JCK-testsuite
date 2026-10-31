/*
 * @test
 * @summary java.util.EventListener API conformance: marker interface with no methods.
 * @run main EventListenerTest
 */

import java.util.EventListener;

public class EventListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MyListener implements EventListener {
    }

    static void testEventListener() {
        check(EventListener.class.isInterface(), "is an interface");
        check(EventListener.class.getDeclaredMethods().length == 0, "marker interface declares no methods");
        check(new MyListener() instanceof EventListener, "is implementable");
    }

    public static void main(String[] args) {
        testEventListener();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
