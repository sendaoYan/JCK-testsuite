/*
 * @test
 * @summary org.w3c.dom.html.HTMLStyleElement conformance: HTMLStyleElement.
 * @run main HTMLStyleElementTest
 */

public class HTMLStyleElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLStyleElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLStyleElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLStyleElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLStyleElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLStyleElement is-a Element");
        check(k.getMethod("getDisabled") != null, "getDisabled()");
        check(k.getMethod("setMedia", String.class) != null, "setMedia(String)");
        check(k.getMethod("getType") != null, "getType()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
