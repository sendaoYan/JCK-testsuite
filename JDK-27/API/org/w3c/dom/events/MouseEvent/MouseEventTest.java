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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(MouseEvent.class.isInterface(), "MouseEvent is an interface");
        check(UIEvent.class.isAssignableFrom(MouseEvent.class), "extends UIEvent");
        check(MouseEvent.class.getMethod("getScreenX") != null, "getScreenX()");
        check(MouseEvent.class.getMethod("getScreenY") != null, "getScreenY()");
        check(MouseEvent.class.getMethod("getButton") != null, "getButton()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.events.MouseEvent a = new org.w3c.dom.events.MouseEvent(){
            public int getScreenX() { return 3; }
            public int getScreenY() { return 3; }
            public int getClientX() { return 3; }
            public int getClientY() { return 3; }
            public boolean getCtrlKey() { return true; }
            public boolean getShiftKey() { return true; }
            public boolean getAltKey() { return true; }
            public boolean getMetaKey() { return true; }
            public short getButton() { return (short)2; }
            public org.w3c.dom.events.EventTarget getRelatedTarget() { return null; }
            public void initMouseEvent(java.lang.String p0, boolean p1, boolean p2, org.w3c.dom.views.AbstractView p3, int p4, int p5, int p6, int p7, int p8, boolean p9, boolean p10, boolean p11, boolean p12, short p13, org.w3c.dom.events.EventTarget p14) { }
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
        check(a != null, "anon impl of MouseEvent");
        check(a.getScreenX() == 3, "getScreenX() returns fixed default");
        check(a.getScreenY() == 3, "getScreenY() returns fixed default");
        check(a.getClientX() == 3, "getClientX() returns fixed default");
        check(a.getClientY() == 3, "getClientY() returns fixed default");
        check(a.getCtrlKey() == true, "getCtrlKey() returns fixed default");
        check(a.getShiftKey() == true, "getShiftKey() returns fixed default");
        check(a.getAltKey() == true, "getAltKey() returns fixed default");
        check(a.getMetaKey() == true, "getMetaKey() returns fixed default");
        check(a.getButton() == (short)2, "getButton() returns fixed default");
        check(a.getRelatedTarget() == null, "getRelatedTarget() returns fixed default");
        a.initMouseEvent("s", true, true, (org.w3c.dom.views.AbstractView) null, 3, 3, 3, 3, 3, true, true, true, true, (short)2, (org.w3c.dom.events.EventTarget) null);
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
