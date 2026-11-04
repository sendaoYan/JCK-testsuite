/*
 * @test
 * @summary javax.swing.event.HyperlinkEvent conformance: white-box structure + black-box behavior.
 * @run main HyperlinkEventTest
 */

import java.lang.reflect.Modifier;
import java.net.URL;
import javax.swing.text.Element;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class HyperlinkEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.HyperlinkEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        Object source = new Object();
        URL url = new URL("https://openjdk.org/");
        javax.swing.event.HyperlinkEvent.EventType activated = javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;

        // 3-arg constructor: description/sourceElement/inputEvent all default to null.
        javax.swing.event.HyperlinkEvent simple = new javax.swing.event.HyperlinkEvent(source, activated, url);
        check(simple.getSource() == source, "getSource round-trip");
        check(simple.getEventType() == activated, "getEventType round-trip");
        check(simple.getURL() == url, "getURL round-trip");
        check(simple.getDescription() == null, "3-arg getDescription null");
        check(simple.getSourceElement() == null, "3-arg getSourceElement null");
        check(simple.getInputEvent() == null, "3-arg getInputEvent null");

        // 4-arg constructor adds a description.
        javax.swing.event.HyperlinkEvent withDesc =
            new javax.swing.event.HyperlinkEvent(source, javax.swing.event.HyperlinkEvent.EventType.ENTERED, url, "openjdk home");
        check(withDesc.getDescription().equals("openjdk home"), "getDescription round-trip");
        check(withDesc.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ENTERED, "getEventType ENTERED");
        check(withDesc.getSourceElement() == null, "4-arg getSourceElement null");

        // 6-arg constructor carries a source Element and InputEvent.
        Element elem = new javax.swing.text.PlainDocument().getDefaultRootElement();
        java.awt.event.InputEvent input = new java.awt.event.MouseEvent(
            new java.awt.Container(), java.awt.event.MouseEvent.MOUSE_CLICKED, 0L, 0, 10, 20, 1, false);
        javax.swing.event.HyperlinkEvent full =
            new javax.swing.event.HyperlinkEvent(source, javax.swing.event.HyperlinkEvent.EventType.EXITED, url, "exit", elem, input);
        check(full.getEventType() == javax.swing.event.HyperlinkEvent.EventType.EXITED, "getEventType EXITED");
        check(full.getSourceElement() == elem, "getSourceElement round-trip");
        check(full.getInputEvent() == input, "getInputEvent round-trip");
        check(full.getURL() == url, "full getURL round-trip");
        check(full.getDescription().equals("exit"), "full getDescription round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.HyperlinkEvent", false, HyperlinkEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEventType"), "method getEventType");
        check(names.contains("getDescription"), "method getDescription");
        check(names.contains("getURL"), "method getURL");
        check(names.contains("getSourceElement"), "method getSourceElement");
    }


    // ---- Part 2b: 5-arg constructor (Element without InputEvent) ----
    static void testExtraContracts() throws Exception {
        Object source5 = new Object();
        URL url5 = new URL("https://openjdk.org/jeps/0");
        Element elem5 = new javax.swing.text.PlainDocument().getDefaultRootElement();
        javax.swing.event.HyperlinkEvent ev5 = new javax.swing.event.HyperlinkEvent(
                source5, javax.swing.event.HyperlinkEvent.EventType.ACTIVATED, url5, "desc5", elem5);
        check(ev5.getSource() == source5, "5-arg source round-trip");
        check(ev5.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED, "5-arg type round-trip");
        check(ev5.getURL() == url5, "5-arg URL round-trip");
        check("desc5".equals(ev5.getDescription()), "5-arg description round-trip");
        check(ev5.getSourceElement() == elem5, "5-arg source element round-trip");
        check(ev5.getInputEvent() == null, "5-arg input event defaults to null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
