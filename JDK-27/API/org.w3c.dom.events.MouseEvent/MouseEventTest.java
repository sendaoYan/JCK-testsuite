/*
 * @test
 * @summary org.w3c.dom.events.MouseEvent conformance: interface extends UIEvent.
 * @run main MouseEventTest
 */

import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.events.UIEvent;

public class MouseEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(MouseEvent.class.isInterface(), "MouseEvent is an interface");
        check(UIEvent.class.isAssignableFrom(MouseEvent.class), "extends UIEvent");
        check(MouseEvent.class.getMethod("getScreenX") != null, "getScreenX()");
        check(MouseEvent.class.getMethod("getScreenY") != null, "getScreenY()");
        check(MouseEvent.class.getMethod("getButton") != null, "getButton()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
