/*
 * @test
 * @summary org.w3c.dom.html.HTMLModElement conformance: HTMLModElement.
 * @run main HTMLModElementTest
 */

public class HTMLModElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLModElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLModElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLModElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLModElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLModElement is-a Element");
        check(k.getMethod("getCite") != null, "getCite()");
        check(k.getMethod("setDateTime", String.class) != null, "setDateTime(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
