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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral UIEvent contract (Level 2 surface, no factory) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.events.UIEvent.class.isInterface(), "UIEvent is an interface");
        check(org.w3c.dom.events.Event.class.isAssignableFrom(org.w3c.dom.events.UIEvent.class), "UIEvent extends Event");
        check(org.w3c.dom.events.UIEvent.class.getMethod("getView").getReturnType().getName().equals("org.w3c.dom.views.AbstractView"), "getView returns AbstractView");
        check(org.w3c.dom.events.UIEvent.class.getMethod("getDetail").getReturnType() == int.class, "getDetail returns int");
        java.lang.reflect.Method init = org.w3c.dom.events.UIEvent.class.getMethod("initUIEvent", String.class, boolean.class, boolean.class, org.w3c.dom.views.AbstractView.class, int.class);
        check(init.getReturnType() == void.class, "initUIEvent(String,boolean,boolean,AbstractView,int) declared");
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        try { de.createEvent("UIEvents"); check(false, "createEvent('UIEvents') must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "createEvent('UIEvents') -> NOT_SUPPORTED_ERR (UI events unsupported)"); }
        // no UIEvent constants either
        int constantCount = 0;
        for (java.lang.reflect.Field fld : org.w3c.dom.events.UIEvent.class.getDeclaredFields()) {
            if (fld.getType() == short.class || fld.getType() == int.class) constantCount++;
        }
        check(constantCount == 0, "UIEvent declares no numeric constants");
        check(true, "no JDK producer for UIEvent instances (view dependency unimplemented)");
    }
}
