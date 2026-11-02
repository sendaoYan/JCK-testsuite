/*
 * @test
 * @summary org.w3c.dom.events.Event conformance: interface, phase constants, methods.
 * @run main EventTest
 */

import org.w3c.dom.events.Event;

public class EventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Event.class.isInterface(), "Event is an interface");
        check(Event.CAPTURING_PHASE == 1, "CAPTURING_PHASE == 1");
        check(Event.AT_TARGET == 2, "AT_TARGET == 2");
        check(Event.BUBBLING_PHASE == 3, "BUBBLING_PHASE == 3");
        check(Event.class.getMethod("getType") != null, "getType()");
        check(Event.class.getMethod("getTarget") != null, "getTarget()");
        check(Event.class.getMethod("getEventPhase") != null, "getEventPhase()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
