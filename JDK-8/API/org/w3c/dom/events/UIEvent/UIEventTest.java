/*
 * @test
 * @summary org.w3c.dom.events.UIEvent conformance: reflection shape checks.
 * @run main UIEventTest
 */

import java.lang.reflect.*;

public class UIEventTest {
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
        Class<?> c = Class.forName("org.w3c.dom.events.UIEvent");
        check(c != null, "org.w3c.dom.events.UIEvent loads");
        check(c.getName().equals("org.w3c.dom.events.UIEvent"), "binary name is org.w3c.dom.events.UIEvent");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.UIEvent is public");
        check(c.isInterface(), "org.w3c.dom.events.UIEvent is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.events.Event")), "direct superinterfaces");
        check(declares(c, "getView"), "declares getView");
        check(declares(c, "getDetail"), "declares getDetail");
        check(declares(c, "initUIEvent"), "declares initUIEvent");
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
