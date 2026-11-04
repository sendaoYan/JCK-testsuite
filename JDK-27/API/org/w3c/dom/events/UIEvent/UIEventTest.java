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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(UIEvent.class.isInterface(), "UIEvent is an interface");
        check(Event.class.isAssignableFrom(UIEvent.class), "extends Event");
        check(UIEvent.class.getMethod("getView") != null, "getView()");
        check(UIEvent.class.getMethod("getDetail") != null, "getDetail()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.events.UIEvent a = new org.w3c.dom.events.UIEvent(){
            public org.w3c.dom.views.AbstractView getView() { return null; }
            public int getDetail() { return 3; }
            public void initUIEvent(java.lang.String p0, boolean p1, boolean p2, org.w3c.dom.views.AbstractView p3, int p4) { }
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
        check(a != null, "anon impl of UIEvent");
        check(a.getView() == null, "getView() returns fixed default");
        check(a.getDetail() == 3, "getDetail() returns fixed default");
        a.initUIEvent("s", true, true, (org.w3c.dom.views.AbstractView) null, 3);
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
