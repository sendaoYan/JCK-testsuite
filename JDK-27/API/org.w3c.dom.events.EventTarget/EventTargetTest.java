/*
 * @test
 * @summary org.w3c.dom.events.EventTarget conformance: interface and methods.
 * @run main EventTargetTest
 */

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class EventTargetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(EventTarget.class.isInterface(), "EventTarget is an interface");
        check(EventTarget.class.getMethod("addEventListener", String.class,
                EventListener.class, boolean.class) != null,
              "addEventListener(String,EventListener,boolean)");
        check(EventTarget.class.getMethod("dispatchEvent", Event.class)
                .getReturnType() == boolean.class, "dispatchEvent(Event) returns boolean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
