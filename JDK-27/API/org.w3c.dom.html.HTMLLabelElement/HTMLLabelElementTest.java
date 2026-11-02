/*
 * @test
 * @summary org.w3c.dom.html.HTMLLabelElement conformance: HTMLLabelElement.
 * @run main HTMLLabelElementTest
 */

public class HTMLLabelElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLLabelElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLLabelElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLLabelElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLLabelElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLLabelElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("getAccessKey") != null, "getAccessKey()");
        check(k.getMethod("getHtmlFor") != null, "getHtmlFor()");
        check(k.getMethod("setHtmlFor", String.class) != null, "setHtmlFor(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
