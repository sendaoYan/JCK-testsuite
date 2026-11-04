/*
 * @test
 * @summary javax.swing.text.html.HTMLFrameHyperlinkEvent conformance: white-box structure + black-box behavior.
 * @run main HTMLFrameHyperlinkEventTest
 */

import java.lang.reflect.Modifier;
import java.net.URL;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

public class HTMLFrameHyperlinkEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLFrameHyperlinkEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.event.HyperlinkEvent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        URL url = new URL("http://example.com/page");
        Object src = "source";
        HTMLDocument doc = new HTMLDocument();
        Element elem = doc.getDefaultRootElement();

        HTMLFrameHyperlinkEvent e1 = new HTMLFrameHyperlinkEvent(src, HyperlinkEvent.EventType.ACTIVATED, url, "main");
        check("main".equals(e1.getTarget()), "ctor4 getTarget");
        check(url.equals(e1.getURL()), "ctor4 getURL");
        check(e1.getEventType() == HyperlinkEvent.EventType.ACTIVATED, "ctor4 getEventType");
        check(src.equals(e1.getSource()), "ctor4 getSource");

        HTMLFrameHyperlinkEvent e2 = new HTMLFrameHyperlinkEvent(src, HyperlinkEvent.EventType.ENTERED, url, "desc", "left");
        check("left".equals(e2.getTarget()), "ctor5 getTarget");
        check("desc".equals(e2.getDescription()), "ctor5 getDescription");

        HTMLFrameHyperlinkEvent e3 = new HTMLFrameHyperlinkEvent(src, HyperlinkEvent.EventType.EXITED, url, elem, "right");
        check("right".equals(e3.getTarget()), "ctorElement getTarget");
        check(elem.equals(e3.getSourceElement()), "ctorElement getSourceElement");

        HTMLFrameHyperlinkEvent e4 = new HTMLFrameHyperlinkEvent(src, HyperlinkEvent.EventType.ACTIVATED, url, "d", elem, "top");
        check("top".equals(e4.getTarget()), "ctor6 getTarget");
        check("d".equals(e4.getDescription()), "ctor6 getDescription");
        check(elem.equals(e4.getSourceElement()), "ctor6 getSourceElement");

        HTMLFrameHyperlinkEvent e5 = new HTMLFrameHyperlinkEvent(src, HyperlinkEvent.EventType.ACTIVATED, url, "d", elem, null, "self");
        check("self".equals(e5.getTarget()), "ctor7 getTarget");
        check(e5.getInputEvent() == null, "ctor7 null input event");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
