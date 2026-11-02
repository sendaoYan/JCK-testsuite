/*
 * @test
 * @summary org.w3c.dom.html.HTMLTitleElement conformance: HTMLTitleElement.
 * @run main HTMLTitleElementTest
 */

public class HTMLTitleElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLTitleElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLTitleElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLTitleElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLTitleElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLTitleElement is-a Element");
        check(k.getMethod("getText") != null, "getText()");
        check(k.getMethod("setText", String.class) != null, "setText(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
