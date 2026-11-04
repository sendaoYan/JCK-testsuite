/*
 * @test
 * @summary javax.swing.event.HyperlinkListener conformance: white-box structure + black-box behavior.
 * @run main HyperlinkListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import java.util.HashSet;
import java.util.Set;

public class HyperlinkListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.HyperlinkListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        Method m = c.getMethod("hyperlinkUpdate", HyperlinkEvent.class);
        check(m.getReturnType() == void.class, "hyperlinkUpdate returns void");
        check(Modifier.isAbstract(m.getModifiers()), "hyperlinkUpdate abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final HyperlinkEvent[] last = new HyperlinkEvent[1];
        HyperlinkListener l = new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) { last[0] = e; }
        };

        Object src = new Object();
        URL url = new URL("http://example.com/path");
        Element el = new PlainDocument().getDefaultRootElement();
        InputEvent ie = new MouseEvent(new Container(), MouseEvent.MOUSE_CLICKED, 0L, 0, 0, 0, 1, false);

        HyperlinkEvent ev = new HyperlinkEvent(src, HyperlinkEvent.EventType.ACTIVATED, url, "description", el, ie);
        l.hyperlinkUpdate(ev);
        check(last[0] == ev, "hyperlinkUpdate fired");
        check(last[0].getSource() == src, "getSource round-trip");
        check(last[0].getEventType() == HyperlinkEvent.EventType.ACTIVATED, "getEventType round-trip");
        check(last[0].getURL() == url, "getURL round-trip");
        check("description".equals(last[0].getDescription()), "getDescription round-trip");
        check(last[0].getSourceElement() == el, "getSourceElement round-trip");
        check(last[0].getInputEvent() == ie, "getInputEvent round-trip");

        HyperlinkEvent entered = new HyperlinkEvent(src, HyperlinkEvent.EventType.ENTERED, url, "d", el, ie);
        l.hyperlinkUpdate(entered);
        check(last[0].getEventType() == HyperlinkEvent.EventType.ENTERED, "getEventType ENTERED");

        HyperlinkEvent exited = new HyperlinkEvent(src, HyperlinkEvent.EventType.EXITED, url, "d", el, ie);
        l.hyperlinkUpdate(exited);
        check(last[0].getEventType() == HyperlinkEvent.EventType.EXITED, "getEventType EXITED");

        check(HyperlinkEvent.EventType.ENTERED != HyperlinkEvent.EventType.EXITED, "ENTERED != EXITED");
        check(HyperlinkEvent.EventType.ACTIVATED != HyperlinkEvent.EventType.ENTERED, "ACTIVATED != ENTERED");
        check("ACTIVATED".equals(HyperlinkEvent.EventType.ACTIVATED.toString()), "ACTIVATED toString");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.HyperlinkListener", false, HyperlinkListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("hyperlinkUpdate"), "method hyperlinkUpdate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
