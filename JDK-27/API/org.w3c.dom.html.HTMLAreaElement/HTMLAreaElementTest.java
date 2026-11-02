/*
 * @test
 * @summary org.w3c.dom.html.HTMLAreaElement conformance: HTMLAreaElement.
 * @run main HTMLAreaElementTest
 */

public class HTMLAreaElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLAreaElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLAreaElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLAreaElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLAreaElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLAreaElement is-a Element");
        check(k.getMethod("getCoords") != null, "getCoords()");
        check(k.getMethod("setNoHref", boolean.class) != null, "setNoHref(boolean)");
        check(k.getMethod("getShape") != null, "getShape()");
        check(k.getMethod("getTabIndex") != null, "getTabIndex()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
