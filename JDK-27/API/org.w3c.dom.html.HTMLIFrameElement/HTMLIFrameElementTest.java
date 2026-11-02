/*
 * @test
 * @summary org.w3c.dom.html.HTMLIFrameElement conformance: HTMLIFrameElement.
 * @run main HTMLIFrameElementTest
 */

public class HTMLIFrameElementTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.html.HTMLIFrameElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLIFrameElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLIFrameElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLIFrameElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLIFrameElement is-a Element");
        check(k.getMethod("getAlign") != null, "getAlign()");
        check(k.getMethod("setSrc", String.class) != null, "setSrc(String)");
        check(k.getMethod("getContentDocument") != null, "getContentDocument()");
        check(k.getMethod("getWidth") != null, "getWidth()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
