/*
 * @test
 * @summary org.w3c.dom.html.HTMLHtmlElement conformance: HTMLHtmlElement.
 * @run main HTMLHtmlElementTest
 */

public class HTMLHtmlElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLHtmlElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLHtmlElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLHtmlElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLHtmlElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLHtmlElement is-a Element");
        check(k.getMethod("getVersion") != null, "getVersion()");
        check(k.getMethod("setVersion", String.class) != null, "setVersion(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
