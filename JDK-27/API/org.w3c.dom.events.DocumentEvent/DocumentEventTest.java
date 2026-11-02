/*
 * @test
 * @summary org.w3c.dom.events.DocumentEvent conformance: interface and createEvent.
 * @run main DocumentEventTest
 */

import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;

public class DocumentEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DocumentEvent.class.isInterface(), "DocumentEvent is an interface");
        check(DocumentEvent.class.getMethod("createEvent", String.class)
                .getReturnType() == Event.class, "createEvent(String) returns Event");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
