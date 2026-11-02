/*
 * @test
 * @summary org.w3c.dom.html.HTMLObjectElement conformance: HTMLObjectElement.
 * @run main HTMLObjectElementTest
 */

public class HTMLObjectElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLObjectElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLObjectElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLObjectElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLObjectElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLObjectElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("getCode") != null, "getCode()");
        check(k.getMethod("setData", String.class) != null, "setData(String)");
        check(k.getMethod("getContentDocument") != null, "getContentDocument()");
        check(k.getMethod("getType") != null, "getType()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
