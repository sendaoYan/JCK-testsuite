/*
 * @test
 * @summary org.w3c.dom.events.MutationEvent conformance: interface extends Event.
 * @run main MutationEventTest
 */

import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;

public class MutationEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(MutationEvent.class.isInterface(), "MutationEvent is an interface");
        check(Event.class.isAssignableFrom(MutationEvent.class), "extends Event");
        check(MutationEvent.MODIFICATION == 1, "MODIFICATION == 1");
        check(MutationEvent.ADDITION == 2, "ADDITION == 2");
        check(MutationEvent.REMOVAL == 3, "REMOVAL == 3");
        check(MutationEvent.class.getMethod("getAttrChange") != null, "getAttrChange()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.events.MutationEvent.MODIFICATION == 1, "MODIFICATION == 1");
        check(org.w3c.dom.events.MutationEvent.ADDITION == 2, "ADDITION == 2");
        check(org.w3c.dom.events.MutationEvent.REMOVAL == 3, "REMOVAL == 3");
        org.w3c.dom.events.MutationEvent a = new org.w3c.dom.events.MutationEvent(){
            public org.w3c.dom.Node getRelatedNode() { return null; }
            public java.lang.String getPrevValue() { return "s"; }
            public java.lang.String getNewValue() { return "s"; }
            public java.lang.String getAttrName() { return "s"; }
            public short getAttrChange() { return (short)2; }
            public void initMutationEvent(java.lang.String p0, boolean p1, boolean p2, org.w3c.dom.Node p3, java.lang.String p4, java.lang.String p5, java.lang.String p6, short p7) { }
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
        check(a != null, "anon impl of MutationEvent");
        check(a.getRelatedNode() == null, "getRelatedNode() returns fixed default");
        check("s".equals(a.getPrevValue()), "getPrevValue() returns fixed default");
        check("s".equals(a.getNewValue()), "getNewValue() returns fixed default");
        check("s".equals(a.getAttrName()), "getAttrName() returns fixed default");
        check(a.getAttrChange() == (short)2, "getAttrChange() returns fixed default");
        a.initMutationEvent("s", true, true, (org.w3c.dom.Node) null, "s", "s", "s", (short)2);
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
