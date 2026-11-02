/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableColElement conformance: HTMLTableColElement.
 * @run main HTMLTableColElementTest
 */

public class HTMLTableColElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLTableColElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLTableColElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLTableColElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLTableColElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLTableColElement is-a Element");
        check(k.getMethod("getAlign") != null, "getAlign()");
        check(k.getMethod("setSpan", int.class) != null, "setSpan(int)");
        check(k.getMethod("getWidth") != null, "getWidth()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
