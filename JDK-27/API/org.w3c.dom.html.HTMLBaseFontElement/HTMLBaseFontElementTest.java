/*
 * @test
 * @summary org.w3c.dom.html.HTMLBaseFontElement conformance: HTMLBaseFontElement.
 * @run main HTMLBaseFontElementTest
 */

public class HTMLBaseFontElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLBaseFontElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLBaseFontElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLBaseFontElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLBaseFontElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLBaseFontElement is-a Element");
        check(k.getMethod("getColor") != null, "getColor()");
        check(k.getMethod("setSize", String.class) != null, "setSize(String)");
        check(k.getMethod("getFace") != null, "getFace()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
