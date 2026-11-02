/*
 * @test
 * @summary org.w3c.dom.html.HTMLMetaElement conformance: HTMLMetaElement.
 * @run main HTMLMetaElementTest
 */

public class HTMLMetaElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLMetaElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLMetaElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLMetaElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLMetaElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLMetaElement is-a Element");
        check(k.getMethod("getContent") != null, "getContent()");
        check(k.getMethod("setHttpEquiv", String.class) != null, "setHttpEquiv(String)");
        check(k.getMethod("getName") != null, "getName()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
