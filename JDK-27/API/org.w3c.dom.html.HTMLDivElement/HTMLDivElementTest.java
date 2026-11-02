/*
 * @test
 * @summary org.w3c.dom.html.HTMLDivElement conformance: HTMLDivElement.
 * @run main HTMLDivElementTest
 */

public class HTMLDivElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLDivElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLDivElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLDivElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLDivElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLDivElement is-a Element");
        check(k.getMethod("getAlign") != null, "getAlign()");
        check(k.getMethod("setAlign", String.class) != null, "setAlign(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
