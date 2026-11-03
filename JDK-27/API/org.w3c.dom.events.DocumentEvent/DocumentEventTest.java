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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentEvent.class.isInterface(), "DocumentEvent is an interface");
        check(DocumentEvent.class.getMethod("createEvent", String.class)
                .getReturnType() == Event.class, "createEvent(String) returns Event");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.events.DocumentEvent a = new org.w3c.dom.events.DocumentEvent(){
            public org.w3c.dom.events.Event createEvent(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of DocumentEvent");
        check(a.createEvent("s") == null, "createEvent() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
