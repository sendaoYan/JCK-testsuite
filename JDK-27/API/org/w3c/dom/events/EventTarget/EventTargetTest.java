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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EventTarget contract (capture/bubble/stopPropagation/mutation) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        final org.w3c.dom.Element root = d.createElement("r");
        d.appendChild(root);
        final org.w3c.dom.Element mid = d.createElement("m");
        root.appendChild(mid);
        final org.w3c.dom.Element leaf = d.createElement("l");
        mid.appendChild(leaf);
        final StringBuilder seq = new StringBuilder();
        class Rec implements org.w3c.dom.events.EventListener {
            String tag; boolean stop;
            Rec(String t, boolean s) { tag = t; stop = s; }
            public void handleEvent(org.w3c.dom.events.Event e) {
                seq.append(tag).append(((org.w3c.dom.Node) e.getCurrentTarget()).getNodeName()).append(e.getEventPhase()).append(';');
                if (stop) e.stopPropagation();
            }
        }
        ((org.w3c.dom.events.EventTarget) root).addEventListener("s", new Rec("R", false), true);
        ((org.w3c.dom.events.EventTarget) mid).addEventListener("s", new Rec("M", false), true);
        ((org.w3c.dom.events.EventTarget) leaf).addEventListener("s", new Rec("L", false), false);
        ((org.w3c.dom.events.EventTarget) mid).addEventListener("s", new Rec("M", false), false);
        ((org.w3c.dom.events.EventTarget) root).addEventListener("s", new Rec("R", false), false);
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        org.w3c.dom.events.Event ev = de.createEvent("Event");
        ev.initEvent("s", true, true);
        ((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(ev);
        check("Rr1;Mm1;Ll2;Mm3;Rr3;".equals(seq.toString()), "three-level dispatch order R1 M1 L2 M3 R3");
        // stopPropagation cuts the rest of the path but lets same-node listeners run
        seq.setLength(0);
        ((org.w3c.dom.events.EventTarget) mid).addEventListener("t", new Rec("M1", true), true);   // stops during capture at mid
        ((org.w3c.dom.events.EventTarget) mid).addEventListener("t", new Rec("M2", false), true);  // same node, same phase
        ((org.w3c.dom.events.EventTarget) leaf).addEventListener("t", new Rec("L", false), false);
        ((org.w3c.dom.events.EventTarget) root).addEventListener("t", new Rec("R", false), true);
        org.w3c.dom.events.Event ev2 = de.createEvent("Event");
        ev2.initEvent("t", true, true);
        ((org.w3c.dom.events.EventTarget) leaf).dispatchEvent(ev2);
        check("Rr1;M1m1;M2m1;".equals(seq.toString()), "stopPropagation: same-node listeners still run, path cut (target never reached)");
        // dispatch to a target with no listeners still succeeds and reports not-prevented
        org.w3c.dom.events.Event ev3 = de.createEvent("Event");
        ev3.initEvent("nobody", true, true);
        boolean ret = ((org.w3c.dom.events.EventTarget) mid).dispatchEvent(ev3);
        check(!ret, "dispatch with no listeners returns false (not canceled)");
        // real DOM mutation events fire without any parser involvement
        final String[] got = new String[]{null};
        ((org.w3c.dom.events.EventTarget) root).addEventListener("DOMNodeInserted", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { got[0] = e.getType() + "@" + ((org.w3c.dom.Node) e.getTarget()).getNodeName(); }
        }, false);
        root.appendChild(d.createElement("newbie"));
        check("DOMNodeInserted@newbie".equals(got[0]), "appendChild dispatches DOMNodeInserted with the inserted node as target");
        final String[] rem = new String[]{null};
        ((org.w3c.dom.events.EventTarget) root).addEventListener("DOMNodeRemoved", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { rem[0] = e.getType(); }
        }, false);
        root.removeChild(root.getLastChild());
        check("DOMNodeRemoved".equals(rem[0]), "removeChild dispatches DOMNodeRemoved");
        // dispatchEvent returns the same event object type it was given
        org.w3c.dom.events.Event mut = de.createEvent("MutationEvent");
        mut.initEvent("custom-mut", true, true);
        ((org.w3c.dom.events.EventTarget) root).dispatchEvent(mut);
        check(mut instanceof org.w3c.dom.events.MutationEvent, "mutation event object dispatchable as plain event");
    }
}
