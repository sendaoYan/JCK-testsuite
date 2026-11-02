/*
 * @test
 * @summary org.w3c.dom.events.UIEvent conformance: interface extends Event.
 * @run main UIEventTest
 */

import org.w3c.dom.events.Event;
import org.w3c.dom.events.UIEvent;

public class UIEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(UIEvent.class.isInterface(), "UIEvent is an interface");
        check(Event.class.isAssignableFrom(UIEvent.class), "extends Event");
        check(UIEvent.class.getMethod("getView") != null, "getView()");
        check(UIEvent.class.getMethod("getDetail") != null, "getDetail()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
