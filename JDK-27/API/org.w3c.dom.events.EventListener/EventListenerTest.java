/*
 * @test
 * @summary org.w3c.dom.events.EventListener conformance: interface and handleEvent.
 * @run main EventListenerTest
 */

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

public class EventListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(EventListener.class.isInterface(), "EventListener is an interface");
        check(EventListener.class.getMethod("handleEvent", Event.class)
                .getReturnType() == void.class, "handleEvent(Event) returns void");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
