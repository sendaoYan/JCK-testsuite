/*
 * @test
 * @summary org.w3c.dom.html.HTMLBRElement conformance: HTMLBRElement.
 * @run main HTMLBRElementTest
 */

public class HTMLBRElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLBRElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLBRElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLBRElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLBRElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLBRElement is-a Element");
        check(k.getMethod("getClear") != null, "getClear()");
        check(k.getMethod("setClear", String.class) != null, "setClear(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
