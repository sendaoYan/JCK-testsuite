/*
 * @test
 * @summary javax.swing.text.html.HTMLFrameHyperlinkEvent conformance: all five
 *          constructors, inherited accessors and lenient null handling
 *          (headless-safe).
 * @run main HTMLFrameHyperlinkEventTest
 */

import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

public class HTMLFrameHyperlinkEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLFrameHyperlinkEvent", false, HTMLFrameHyperlinkEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.event.HyperlinkEvent"), "superclass");
        check(Modifier.isPublic(c.getModifiers()) == true, "public modifier");
        check(Modifier.isProtected(c.getModifiers()) == false, "protected modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getConstructors().length == 5, "five public constructors");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getTarget"), "method getTarget");
    }

    // ---- Part 2: constructor matrix ----
    static void testConstructors() throws Exception {
        Object src = new Object();
        URL u = new URL("http://example.com/x.html");
        HyperlinkEvent.EventType type = HyperlinkEvent.EventType.ACTIVATED;
        Element elem = null; // real elements are covered in HTMLDocumentTest

        HTMLFrameHyperlinkEvent e1 = new HTMLFrameHyperlinkEvent(src, type, u, "tf");
        check("tf".equals(e1.getTarget()), "ctor(url,target) getTarget");
        check(e1.getSource() == src, "ctor1 source");
        check(u.equals(e1.getURL()), "ctor1 url");
        check(type == e1.getEventType(), "ctor1 event type");
        check(e1.getDescription() == null, "ctor1 description null");
        check(e1.getSourceElement() == null, "ctor1 source element null");

        HTMLFrameHyperlinkEvent e2 = new HTMLFrameHyperlinkEvent(src, type, u, "desc", "tf");
        check("desc".equals(e2.getDescription()), "ctor(url,desc,target) description");
        check("tf".equals(e2.getTarget()), "ctor2 target");
        check(e2.getSourceElement() == null, "ctor2 source element null");

        HTMLFrameHyperlinkEvent e3 = new HTMLFrameHyperlinkEvent(src, type, u, elem, "tf");
        check("tf".equals(e3.getTarget()), "ctor(url,elem,target) target");
        check(e3.getSourceElement() == elem, "ctor3 source element");
        check(e3.getDescription() == null, "ctor3 description null");

        HTMLFrameHyperlinkEvent e4 = new HTMLFrameHyperlinkEvent(src, type, u, "d", elem, "tf");
        check("d".equals(e4.getDescription()), "ctor(url,desc,elem,target) description");
        check(e4.getSourceElement() == elem, "ctor4 source element");
        check("tf".equals(e4.getTarget()), "ctor4 target");

        MouseEvent me = new MouseEvent(new javax.swing.JLabel(), MouseEvent.MOUSE_CLICKED, 0L, 0, 1, 2, 1, false);
        HTMLFrameHyperlinkEvent e5 = new HTMLFrameHyperlinkEvent(src, type, u, "d", elem, me, "tf");
        check("tf".equals(e5.getTarget()), "ctor(url,desc,elem,inputEvent,target) target");
        check("d".equals(e5.getDescription()), "ctor5 description");
        check(e5 instanceof HyperlinkEvent, "ctor5 is a HyperlinkEvent");
        check(e5.getEventType() == type, "ctor5 event type");

        // lenient nulls tolerated by the superclass
        HTMLFrameHyperlinkEvent n1 = new HTMLFrameHyperlinkEvent(src, type, null, "tf");
        check(n1.getURL() == null, "null url kept as null");
        HTMLFrameHyperlinkEvent n2 = new HTMLFrameHyperlinkEvent(src, null, u, "tf");
        check(n2.getEventType() == null, "null event type kept as null");
        boolean nullSrc = false;
        try { new HTMLFrameHyperlinkEvent(null, type, u, "tf"); }
        catch (IllegalArgumentException e) { nullSrc = true; }
        check(nullSrc, "null source rejected with IllegalArgumentException");
        HTMLFrameHyperlinkEvent n4 = new HTMLFrameHyperlinkEvent(src, type, u, (String) null, "tf");
        check(n4.getDescription() == null, "null description kept");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
