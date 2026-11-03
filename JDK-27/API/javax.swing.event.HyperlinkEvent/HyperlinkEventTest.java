/*
 * @test
 * @summary javax.swing.event.HyperlinkEvent conformance: white-box structure + black-box behavior.
 * @run main HyperlinkEventTest
 */

import java.lang.reflect.Modifier;
import java.net.URL;
import javax.swing.text.Element;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
