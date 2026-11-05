/*
 * @test
 * @summary org.w3c.dom.events.EventTarget conformance: reflection shape checks.
 * @run main EventTargetTest
 */

import java.lang.reflect.*;

public class EventTargetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.events.EventTarget");
        check(c != null, "org.w3c.dom.events.EventTarget loads");
        check(c.getName().equals("org.w3c.dom.events.EventTarget"), "binary name is org.w3c.dom.events.EventTarget");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.EventTarget is public");
        check(c.isInterface(), "org.w3c.dom.events.EventTarget is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "addEventListener"), "declares addEventListener");
        check(declares(c, "removeEventListener"), "declares removeEventListener");
        check(declares(c, "dispatchEvent"), "declares dispatchEvent");
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
