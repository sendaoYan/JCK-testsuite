/*
 * @test
 * @summary org.w3c.dom.html.HTMLQuoteElement conformance: HTMLQuoteElement.
 * @run main HTMLQuoteElementTest
 */

public class HTMLQuoteElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLQuoteElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLQuoteElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLQuoteElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLQuoteElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLQuoteElement is-a Element");
        check(k.getMethod("getCite") != null, "getCite()");
        check(k.getMethod("setCite", String.class) != null, "setCite(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
