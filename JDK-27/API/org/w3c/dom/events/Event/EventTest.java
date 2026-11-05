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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Event contract (init/getters/phases/preventDefault) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        final org.w3c.dom.Element root = d.createElement("r");
        d.appendChild(root);
        final org.w3c.dom.Element leaf = d.createElement("leaf");
        root.appendChild(leaf);
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        org.w3c.dom.events.Event ev = de.createEvent("Event");
        ev.initEvent("ping", true, true);
        check("ping".equals(ev.getType()), "initEvent sets type");
        check(ev.getBubbles() && ev.getCancelable(), "initEvent sets bubbles/cancelable");
        ev.initEvent("pong", false, false);
        check("pong".equals(ev.getType()) && !ev.getBubbles() && !ev.getCancelable(), "re-initEvent overwrites flags");
        check(ev.getTarget() == null && ev.getCurrentTarget() == null, "target/currentTarget null outside dispatch");
        check(ev.getEventPhase() == org.w3c.dom.events.Event.AT_TARGET || ev.getEventPhase() == 0, "phase outside dispatch is AT_TARGET(2) or 0");
        check(org.w3c.dom.events.Event.CAPTURING_PHASE == 1 && org.w3c.dom.events.Event.AT_TARGET == 2 && org.w3c.dom.events.Event.BUBBLING_PHASE == 3, "phase constants 1/2/3");
        // full capture-target-bubble path with phase recording
        final StringBuilder path = new StringBuilder();
        org.w3c.dom.events.EventListener cap = new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { path.append("cap").append(e.getEventPhase()); }
        };
        org.w3c.dom.events.EventListener bub = new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { path.append("|bub").append(e.getEventPhase()); }
        };
        ((org.w3c.dom.events.EventTarget) root).addEventListener("p", cap, true);
        ((org.w3c.dom.events.EventTarget) root).addEventListener("p", bub, false);
        final int[] targetPhase = new int[]{-1};
        final String[] who = new String[2];
        ((org.w3c.dom.events.EventTarget) leaf).addEventListener("p", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) {
                targetPhase[0] = e.getEventPhase();
                who[0] = ((org.w3c.dom.Node) e.getTarget()).getNodeName();
                who[1] = ((org.w3c.dom.Node) e.getCurrentTarget()).getNodeName();
                path.append("|tgt").append(e.getEventPhase());
            }
        }, false);
        org.w3c.dom.events.Event pe = de.createEvent("Event");
        pe.initEvent("p", true, true);
        boolean ret = ((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(pe);
        check("cap1|tgt2|bub3".equals(path.toString()), "dispatch order capture(1) -> target(2) -> bubble(3)");
        check(targetPhase[0] == org.w3c.dom.events.Event.AT_TARGET, "target listener runs at AT_TARGET");
        check("leaf".equals(who[0]) && "leaf".equals(who[1]), "target/currentTarget coincide at the target node");
        check(ret == false, "Xerces dispatchEvent returns false when no listener prevented (inverted vs spec, both JDKs)");
        // preventDefault flips the return value
        final boolean[] prevent = new boolean[]{true};
        ((org.w3c.dom.events.EventTarget) leaf).addEventListener("q", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { if (prevent[0]) e.preventDefault(); }
        }, false);
        org.w3c.dom.events.Event qe = de.createEvent("Event");
        qe.initEvent("q", true, true);
        check(((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(qe) == true, "preventDefault makes dispatchEvent return true (Xerces)");
        prevent[0] = false;
        org.w3c.dom.events.Event qe2 = de.createEvent("Event");
        qe2.initEvent("q", true, true);
        check(((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(qe2) == false, "non-canceled dispatch returns false");
        // non-bubbling: ancestors only see the capture phase
        final StringBuilder nb = new StringBuilder();
        ((org.w3c.dom.events.EventTarget) root).addEventListener("nb", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { nb.append(e.getEventPhase()); }
        }, true);
        ((org.w3c.dom.events.EventTarget) root).addEventListener("nb", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { nb.append(e.getEventPhase()); }
        }, false);
        org.w3c.dom.events.Event nbe = de.createEvent("Event");
        nbe.initEvent("nb", false, true);
        ((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(nbe);
        check("1".equals(nb.toString()), "bubbles=false: ancestor sees capture phase only");
        // timestamp visible during dispatch
        final long[] ts = new long[]{0};
        ((org.w3c.dom.events.EventTarget) leaf).addEventListener("ts", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { ts[0] = e.getTimeStamp(); }
        }, false);
        org.w3c.dom.events.Event tse = de.createEvent("Event");
        tse.initEvent("ts", true, true);
        ((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(tse);
        check(ts[0] > 0, "getTimeStamp positive during dispatch");
        // Level 3 members absent
        boolean hasImmediate = true;
        try { org.w3c.dom.events.Event.class.getMethod("stopImmediatePropagation"); }
        catch (NoSuchMethodException expected) { hasImmediate = false; }
        check(!hasImmediate, "stopImmediatePropagation absent (Level 2)");
        boolean hasDefaultPrevented = true;
        try { org.w3c.dom.events.Event.class.getMethod("getDefaultPrevented"); }
        catch (NoSuchMethodException expected) { hasDefaultPrevented = false; }
        check(!hasDefaultPrevented, "getDefaultPrevented absent (Level 2)");
        // dispatching an uninitialized event
        try { ((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(de.createEvent("Event")); check(false, "uninitialized dispatch must throw"); }
        catch (org.w3c.dom.events.EventException ex) { check(ex.code == org.w3c.dom.events.EventException.UNSPECIFIED_EVENT_TYPE_ERR, "uninitialized dispatch -> EventException UNSPECIFIED_EVENT_TYPE_ERR(0)"); }
    }
}
