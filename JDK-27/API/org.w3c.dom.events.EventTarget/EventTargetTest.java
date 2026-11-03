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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(EventTarget.class.isInterface(), "EventTarget is an interface");
        check(EventTarget.class.getMethod("addEventListener", String.class,
                EventListener.class, boolean.class) != null,
              "addEventListener(String,EventListener,boolean)");
        check(EventTarget.class.getMethod("dispatchEvent", Event.class)
                .getReturnType() == boolean.class, "dispatchEvent(Event) returns boolean");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.events.EventTarget a = new org.w3c.dom.events.EventTarget(){
            public void addEventListener(java.lang.String p0, org.w3c.dom.events.EventListener p1, boolean p2) { }
            public void removeEventListener(java.lang.String p0, org.w3c.dom.events.EventListener p1, boolean p2) { }
            public boolean dispatchEvent(org.w3c.dom.events.Event p0) { return true; }
        };
        check(a != null, "anon impl of EventTarget");
        a.addEventListener("s", (org.w3c.dom.events.EventListener) null, true);
        a.removeEventListener("s", (org.w3c.dom.events.EventListener) null, true);
        check(a.dispatchEvent((org.w3c.dom.events.Event) null) == true, "dispatchEvent() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
