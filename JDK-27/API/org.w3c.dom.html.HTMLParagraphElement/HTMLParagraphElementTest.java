/*
 * @test
 * @summary org.w3c.dom.html.HTMLParagraphElement conformance: HTMLParagraphElement.
 * @run main HTMLParagraphElementTest
 */

public class HTMLParagraphElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLParagraphElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLParagraphElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLParagraphElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLParagraphElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLParagraphElement is-a Element");
        check(k.getMethod("getAlign") != null, "getAlign()");
        check(k.getMethod("setAlign", String.class) != null, "setAlign(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
