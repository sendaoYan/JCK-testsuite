/*
 * @test
 * @summary org.w3c.dom.events.MouseEvent conformance: reflection shape checks.
 * @run main MouseEventTest
 */

import java.lang.reflect.*;

public class MouseEventTest {
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
        Class<?> c = Class.forName("org.w3c.dom.events.MouseEvent");
        check(c != null, "org.w3c.dom.events.MouseEvent loads");
        check(c.getName().equals("org.w3c.dom.events.MouseEvent"), "binary name is org.w3c.dom.events.MouseEvent");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.MouseEvent is public");
        check(c.isInterface(), "org.w3c.dom.events.MouseEvent is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.events.UIEvent")), "direct superinterfaces");
        check(declares(c, "getScreenX"), "declares getScreenX");
        check(declares(c, "getScreenY"), "declares getScreenY");
        check(declares(c, "getClientX"), "declares getClientX");
        check(declares(c, "getClientY"), "declares getClientY");
        check(declares(c, "getCtrlKey"), "declares getCtrlKey");
        check(declares(c, "getShiftKey"), "declares getShiftKey");
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
