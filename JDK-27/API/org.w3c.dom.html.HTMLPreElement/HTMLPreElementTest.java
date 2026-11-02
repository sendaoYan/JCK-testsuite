/*
 * @test
 * @summary org.w3c.dom.html.HTMLPreElement conformance: HTMLPreElement.
 * @run main HTMLPreElementTest
 */

public class HTMLPreElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLPreElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLPreElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLPreElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLPreElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLPreElement is-a Element");
        check(k.getMethod("getWidth") != null, "getWidth()");
        check(k.getMethod("setWidth", int.class) != null, "setWidth(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
