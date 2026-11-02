/*
 * @test
 * @summary org.w3c.dom.html.HTMLBodyElement conformance: HTMLBodyElement.
 * @run main HTMLBodyElementTest
 */

public class HTMLBodyElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLBodyElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLBodyElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLBodyElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLBodyElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLBodyElement is-a Element");
        check(k.getMethod("getALink") != null, "getALink()");
        check(k.getMethod("setBgColor", String.class) != null, "setBgColor(String)");
        check(k.getMethod("getText") != null, "getText()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
