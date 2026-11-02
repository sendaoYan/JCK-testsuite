/*
 * @test
 * @summary org.w3c.dom.html.HTMLLinkElement conformance: HTMLLinkElement.
 * @run main HTMLLinkElementTest
 */

public class HTMLLinkElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLLinkElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLLinkElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLLinkElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLLinkElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLLinkElement is-a Element");
        check(k.getMethod("getDisabled") != null, "getDisabled()");
        check(k.getMethod("setHref", String.class) != null, "setHref(String)");
        check(k.getMethod("getMedia") != null, "getMedia()");
        check(k.getMethod("getRel") != null, "getRel()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
