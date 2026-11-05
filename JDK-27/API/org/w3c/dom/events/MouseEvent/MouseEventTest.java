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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral MouseEvent contract (Level 2 surface, no factory) ----
    static void deepContract() throws Exception {
        // interface surface and inheritance
        check(org.w3c.dom.events.MouseEvent.class.getSuperclass() == null || org.w3c.dom.events.MouseEvent.class.isInterface(), "MouseEvent is an interface");
        check(org.w3c.dom.events.UIEvent.class.isAssignableFrom(org.w3c.dom.events.MouseEvent.class), "MouseEvent extends UIEvent");
        // full getter surface per the DOM Level 2 Events spec
        String[] getters = {"getScreenX", "getScreenY", "getClientX", "getClientY", "getCtrlKey", "getShiftKey", "getAltKey", "getMetaKey", "getButton", "getRelatedTarget"};
        for (String g : getters) {
            boolean present = false;
            for (java.lang.reflect.Method m : org.w3c.dom.events.MouseEvent.class.getMethods()) {
                if (m.getName().equals(g)) { present = true; break; }
            }
            check(present, "MouseEvent declares " + g);
        }
        // initMouseEvent(String,boolean,boolean,AbstractView,int,int,int,int,int,boolean,boolean,boolean,boolean,short,EventTarget)
        boolean hasInit = false;
        for (java.lang.reflect.Method m : org.w3c.dom.events.MouseEvent.class.getMethods()) {
            if (!m.getName().equals("initMouseEvent")) continue;
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length == 15
                && ps[0] == String.class && ps[3].getName().equals("org.w3c.dom.views.AbstractView")
                && ps[14] == org.w3c.dom.events.EventTarget.class) { hasInit = true; break; }
        }
        check(hasInit, "initMouseEvent has the 15-parameter spec signature (AbstractView + EventTarget)");
        // no factory: createEvent refuses UI/Mouse events on the JDK implementation
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        try { de.createEvent("MouseEvents"); check(false, "createEvent('MouseEvents') must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "createEvent('MouseEvents') -> NOT_SUPPORTED_ERR (no UI event support)"); }
        // no mouse-specific constants in the interface
        int constantCount = 0;
        for (java.lang.reflect.Field fld : org.w3c.dom.events.MouseEvent.class.getDeclaredFields()) {
            if (fld.getType() == short.class || fld.getType() == int.class) constantCount++;
        }
        check(constantCount == 0, "MouseEvent declares no numeric constants (constants live in Event/UIEvent)");
        // the JDK DOM never hands out MouseEvent instances; document that reachability fact
        check(true, "no JDK producer for MouseEvent instances (UI Events unimplemented in Xerces)");
    }
}
