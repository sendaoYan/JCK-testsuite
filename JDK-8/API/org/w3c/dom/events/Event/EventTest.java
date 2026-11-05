/*
 * @test
 * @summary org.w3c.dom.events.Event conformance: reflection shape checks.
 * @run main EventTest
 */

import java.lang.reflect.*;

public class EventTest {
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
        Class<?> c = Class.forName("org.w3c.dom.events.Event");
        check(c != null, "org.w3c.dom.events.Event loads");
        check(c.getName().equals("org.w3c.dom.events.Event"), "binary name is org.w3c.dom.events.Event");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.Event is public");
        check(c.isInterface(), "org.w3c.dom.events.Event is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "getTarget"), "declares getTarget");
        check(declares(c, "getCurrentTarget"), "declares getCurrentTarget");
        check(declares(c, "getEventPhase"), "declares getEventPhase");
        check(declares(c, "getBubbles"), "declares getBubbles");
        check(declares(c, "getCancelable"), "declares getCancelable");
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
