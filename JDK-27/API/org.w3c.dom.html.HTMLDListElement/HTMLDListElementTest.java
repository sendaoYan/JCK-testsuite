/*
 * @test
 * @summary org.w3c.dom.html.HTMLDListElement conformance: HTMLDListElement.
 * @run main HTMLDListElementTest
 */

public class HTMLDListElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLDListElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLDListElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLDListElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLDListElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLDListElement is-a Element");
        check(k.getMethod("getCompact") != null, "getCompact()");
        check(k.getMethod("setCompact", boolean.class) != null, "setCompact(boolean)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
