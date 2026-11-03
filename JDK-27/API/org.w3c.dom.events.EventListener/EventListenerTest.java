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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(EventListener.class.isInterface(), "EventListener is an interface");
        check(EventListener.class.getMethod("handleEvent", Event.class)
                .getReturnType() == void.class, "handleEvent(Event) returns void");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.events.EventListener a = new org.w3c.dom.events.EventListener(){
            public void handleEvent(org.w3c.dom.events.Event p0) { }
        };
        check(a != null, "anon impl of EventListener");
        a.handleEvent((org.w3c.dom.events.Event) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
