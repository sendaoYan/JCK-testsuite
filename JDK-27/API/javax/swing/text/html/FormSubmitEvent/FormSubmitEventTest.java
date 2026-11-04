/*
 * @test
 * @summary javax.swing.text.html.FormSubmitEvent conformance: white-box structure + black-box behavior.
 * @run main/othervm --add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED FormSubmitEventTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLDocument;

public class FormSubmitEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.FormSubmitEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLFrameHyperlinkEvent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(Modifier.isFinal(mods) == true, "final modifier");
    }

    static FormSubmitEvent make(String url, String method, String data) throws Exception {
        HTMLDocument doc = new HTMLDocument();
        Element root = doc.getDefaultRootElement();
        FormSubmitEvent.MethodType mt = "POST".equals(method)
                ? FormSubmitEvent.MethodType.POST : FormSubmitEvent.MethodType.GET;
        Constructor<?> ctor = FormSubmitEvent.class.getDeclaredConstructor(
                Object.class, HyperlinkEvent.EventType.class, URL.class,
                Element.class, String.class, FormSubmitEvent.MethodType.class, String.class);
        ctor.setAccessible(true);
        return (FormSubmitEvent) ctor.newInstance(
                "src", HyperlinkEvent.EventType.ACTIVATED,
                new URL(url), root, "main", mt, data);
    }

    static void testBlackBox() throws Exception {
        FormSubmitEvent ev = make("http://example.com/submit", "POST", "a=1&b=2");
        check(ev.getMethod() == FormSubmitEvent.MethodType.POST, "getMethod POST");
        check("a=1&b=2".equals(ev.getData()), "getData");
        check(ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED, "getEventType ACTIVATED");
        check("http://example.com/submit".equals(ev.getURL().toExternalForm()), "getURL");
        check("main".equals(ev.getTarget()), "getTarget");
        check("src".equals(ev.getSource()), "getSource");
        check(ev.getSourceElement() != null && ev.getSourceElement().getName().equals("html"), "getSourceElement");

        FormSubmitEvent getEv = make("http://example.com/search", "GET", "q=java");
        check(getEv.getMethod() == FormSubmitEvent.MethodType.GET, "getMethod GET");
        check("q=java".equals(getEv.getData()), "getData GET");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
