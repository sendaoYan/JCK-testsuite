/*
 * @test
 * @summary org.w3c.dom.html.HTMLDOMImplementation conformance: DOMImplementation subtype.
 * @run main HTMLDOMImplementationTest
 */

public class HTMLDOMImplementationTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLDOMImplementation");
        check(k.isInterface(), "org.w3c.dom.html.HTMLDOMImplementation is an interface");
        check(L("org.w3c.dom.DOMImplementation").isAssignableFrom(k), "HTMLDOMImplementation extends DOMImplementation");
        check(k.getInterfaces().length >= 1, "HTMLDOMImplementation has superinterface");
        check(k.getMethod("createHTMLDocument", String.class) != null, "createHTMLDocument(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
