/*
 * @test
 * @summary org.w3c.dom.events.DocumentEvent conformance: reflection shape checks.
 * @run main DocumentEventTest
 */

import java.lang.reflect.*;

public class DocumentEventTest {
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
        Class<?> c = Class.forName("org.w3c.dom.events.DocumentEvent");
        check(c != null, "org.w3c.dom.events.DocumentEvent loads");
        check(c.getName().equals("org.w3c.dom.events.DocumentEvent"), "binary name is org.w3c.dom.events.DocumentEvent");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.DocumentEvent is public");
        check(c.isInterface(), "org.w3c.dom.events.DocumentEvent is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "createEvent"), "declares createEvent");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentEvent contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        check(de != null, "Document casts to DocumentEvent (Xerces combines both)");
        org.w3c.dom.events.Event e1 = de.createEvent("Event");
        check(e1 != null, "createEvent('Event') non-null");
        check(de.createEvent("Events") != null, "createEvent('Events') alias accepted");
        org.w3c.dom.events.Event me = de.createEvent("MutationEvent");
        check(me instanceof org.w3c.dom.events.MutationEvent, "createEvent('MutationEvent') type");
        check(de.createEvent("MutationEvents") instanceof org.w3c.dom.events.MutationEvent, "createEvent('MutationEvents') alias");
        try { de.createEvent("UIEvents"); check(false, "createEvent('UIEvents') must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "createEvent('UIEvents') -> NOT_SUPPORTED_ERR(9)"); }
        try { de.createEvent("MouseEvents"); check(false, "createEvent('MouseEvents') must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "createEvent('MouseEvents') -> NOT_SUPPORTED_ERR(9)"); }
        try { de.createEvent("bogus"); check(false, "createEvent(bogus) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "createEvent(bogus) -> NOT_SUPPORTED_ERR(9)"); }
        boolean npe = false;
        try { de.createEvent(null); } catch (NullPointerException expected) { npe = true; }
        check(npe, "createEvent(null) -> NullPointerException");
        // fresh events start un-initialized
        check(e1.getType() == null || e1.getType().isEmpty(), "fresh event has empty type until initEvent");
        // two documents produce independent event objects
        org.w3c.dom.Document d2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.events.Event e2 = ((org.w3c.dom.events.DocumentEvent) d2).createEvent("Event");
        check(e1 != e2, "createEvent returns a new object per call");
        // DOM Level 3 extensions are absent on both JDKs
        boolean hasCanDispatch = true;
        try { org.w3c.dom.events.DocumentEvent.class.getMethod("canDispatch", String.class, String.class); }
        catch (NoSuchMethodException expected) { hasCanDispatch = false; }
        check(!hasCanDispatch, "canDispatch absent (Events stuck at Level 2)");
        boolean hasCreateEvent3 = true;
        try { org.w3c.dom.events.DocumentEvent.class.getMethod("createEvent", String.class); }
        catch (NoSuchMethodException expected) { hasCreateEvent3 = false; }
        check(hasCreateEvent3, "createEvent(String) declared");
    }
}
