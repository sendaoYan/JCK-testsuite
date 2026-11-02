/*
 * @test
 * @summary org.w3c.dom.html.HTMLAnchorElement conformance: HTMLAnchorElement.
 * @run main HTMLAnchorElementTest
 */

public class HTMLAnchorElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLAnchorElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLAnchorElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLAnchorElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLAnchorElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLAnchorElement is-a Element");
        check(k.getMethod("getHref") != null, "getHref()");
        check(k.getMethod("setHref", String.class) != null, "setHref(String)");
        check(k.getMethod("getTabIndex") != null, "getTabIndex()");
        check(k.getMethod("blur") != null, "blur()");
        check(k.getMethod("focus") != null, "focus()");
        check(k.getMethod("getAccessKey") != null, "getAccessKey()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
