/*
 * @test
 * @summary org.w3c.dom.html.HTMLUListElement conformance: HTMLUListElement.
 * @run main HTMLUListElementTest
 */

public class HTMLUListElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLUListElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLUListElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLUListElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLUListElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLUListElement is-a Element");
        check(k.getMethod("getCompact") != null, "getCompact()");
        check(k.getMethod("getType") != null, "getType()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
