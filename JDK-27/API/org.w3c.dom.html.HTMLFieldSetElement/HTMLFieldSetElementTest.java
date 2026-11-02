/*
 * @test
 * @summary org.w3c.dom.html.HTMLFieldSetElement conformance: HTMLFieldSetElement.
 * @run main HTMLFieldSetElementTest
 */

public class HTMLFieldSetElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLFieldSetElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLFieldSetElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLFieldSetElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLFieldSetElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLFieldSetElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
