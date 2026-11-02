/*
 * @test
 * @summary org.w3c.dom.events.MutationEvent conformance: interface extends Event.
 * @run main MutationEventTest
 */

import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;

public class MutationEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(MutationEvent.class.isInterface(), "MutationEvent is an interface");
        check(Event.class.isAssignableFrom(MutationEvent.class), "extends Event");
        check(MutationEvent.MODIFICATION == 1, "MODIFICATION == 1");
        check(MutationEvent.ADDITION == 2, "ADDITION == 2");
        check(MutationEvent.REMOVAL == 3, "REMOVAL == 3");
        check(MutationEvent.class.getMethod("getAttrChange") != null, "getAttrChange()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
