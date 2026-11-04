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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Event.class.isInterface(), "Event is an interface");
        check(Event.CAPTURING_PHASE == 1, "CAPTURING_PHASE == 1");
        check(Event.AT_TARGET == 2, "AT_TARGET == 2");
        check(Event.BUBBLING_PHASE == 3, "BUBBLING_PHASE == 3");
        check(Event.class.getMethod("getType") != null, "getType()");
        check(Event.class.getMethod("getTarget") != null, "getTarget()");
        check(Event.class.getMethod("getEventPhase") != null, "getEventPhase()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.events.Event.CAPTURING_PHASE == 1, "CAPTURING_PHASE == 1");
        check(org.w3c.dom.events.Event.AT_TARGET == 2, "AT_TARGET == 2");
        check(org.w3c.dom.events.Event.BUBBLING_PHASE == 3, "BUBBLING_PHASE == 3");
        org.w3c.dom.events.Event a = new org.w3c.dom.events.Event(){
            public java.lang.String getType() { return "s"; }
            public org.w3c.dom.events.EventTarget getTarget() { return null; }
            public org.w3c.dom.events.EventTarget getCurrentTarget() { return null; }
            public short getEventPhase() { return (short)2; }
            public boolean getBubbles() { return true; }
            public boolean getCancelable() { return true; }
            public long getTimeStamp() { return 4L; }
            public void stopPropagation() { }
            public void preventDefault() { }
            public void initEvent(java.lang.String p0, boolean p1, boolean p2) { }
        };
        check(a != null, "anon impl of Event");
        check("s".equals(a.getType()), "getType() returns fixed default");
        check(a.getTarget() == null, "getTarget() returns fixed default");
        check(a.getCurrentTarget() == null, "getCurrentTarget() returns fixed default");
        check(a.getEventPhase() == (short)2, "getEventPhase() returns fixed default");
        check(a.getBubbles() == true, "getBubbles() returns fixed default");
        check(a.getCancelable() == true, "getCancelable() returns fixed default");
        check(a.getTimeStamp() == 4L, "getTimeStamp() returns fixed default");
        a.stopPropagation();
        a.preventDefault();
        a.initEvent("s", true, true);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
