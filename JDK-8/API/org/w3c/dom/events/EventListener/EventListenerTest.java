/*
 * @test
 * @summary org.w3c.dom.events.EventListener conformance: reflection shape checks.
 * @run main EventListenerTest
 */

import java.lang.reflect.*;

public class EventListenerTest {
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
        Class<?> c = Class.forName("org.w3c.dom.events.EventListener");
        check(c != null, "org.w3c.dom.events.EventListener loads");
        check(c.getName().equals("org.w3c.dom.events.EventListener"), "binary name is org.w3c.dom.events.EventListener");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.EventListener is public");
        check(c.isInterface(), "org.w3c.dom.events.EventListener is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "handleEvent"), "declares handleEvent");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EventListener contract (registration/removal/thrown errors) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        final org.w3c.dom.Element e = d.createElement("e");
        d.appendChild(e);
        final int[] calls = new int[]{0};
        final org.w3c.dom.events.Event[] received = new org.w3c.dom.events.Event[]{null};
        org.w3c.dom.events.EventListener l = new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event evt) { calls[0]++; received[0] = evt; }
        };
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        org.w3c.dom.events.Event ev = de.createEvent("Event");
        ev.initEvent("go", true, true);
        ((org.w3c.dom.events.EventTarget) e).addEventListener("go", l, false);
        ((org.w3c.dom.events.EventTarget) e).dispatchEvent(ev);
        check(calls[0] == 1, "registered listener invoked");
        check(received[0] != null && "go".equals(received[0].getType()), "listener receives the dispatched event object");
        // duplicate registration with identical flags does not double-fire
        ((org.w3c.dom.events.EventTarget) e).addEventListener("go", l, false);
        ((org.w3c.dom.events.EventTarget) e).dispatchEvent(ev);
        check(calls[0] == 2, "duplicate addEventListener(same flags) keeps one registration");
        // removal with a mismatched capture flag is a no-op
        ((org.w3c.dom.events.EventTarget) e).removeEventListener("go", l, true);
        ((org.w3c.dom.events.EventTarget) e).dispatchEvent(ev);
        check(calls[0] == 3, "removeEventListener with wrong capture flag is a no-op");
        // removal with matching flags stops delivery
        ((org.w3c.dom.events.EventTarget) e).removeEventListener("go", l, false);
        ((org.w3c.dom.events.EventTarget) e).dispatchEvent(ev);
        check(calls[0] == 3, "removeEventListener with right flags stops delivery");
        // unknown type removal never throws
        ((org.w3c.dom.events.EventTarget) e).removeEventListener("nope", l, false);
        check(true, "removeEventListener(unknown type) is a silent no-op");
        // a listener that throws does not break dispatch
        ((org.w3c.dom.events.EventTarget) e).addEventListener("boom", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event evt) { throw new RuntimeException("listener boom"); }
        }, false);
        org.w3c.dom.events.Event be = de.createEvent("Event");
        be.initEvent("boom", true, true);
        boolean returned = ((org.w3c.dom.events.EventTarget) e).dispatchEvent(be);
        check(!returned, "throwing listener is swallowed; dispatch still returns (Xerces)");
        // two distinct listeners on the same type both fire
        final int[] a = new int[]{0};
        final int[] b = new int[]{0};
        ((org.w3c.dom.events.EventTarget) e).addEventListener("two", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event evt) { a[0]++; }
        }, false);
        ((org.w3c.dom.events.EventTarget) e).addEventListener("two", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event evt) { b[0]++; }
        }, false);
        org.w3c.dom.events.Event te = de.createEvent("Event");
        te.initEvent("two", true, true);
        ((org.w3c.dom.events.EventTarget) e).dispatchEvent(te);
        check(a[0] == 1 && b[0] == 1, "distinct listeners each fire once");
    }
}
