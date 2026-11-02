/*
 * @test
 * @summary org.w3c.dom.html.HTMLLegendElement conformance: HTMLLegendElement.
 * @run main HTMLLegendElementTest
 */

public class HTMLLegendElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLLegendElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLLegendElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLLegendElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLLegendElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLLegendElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("getAccessKey") != null, "getAccessKey()");
        check(k.getMethod("getAlign") != null, "getAlign()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
