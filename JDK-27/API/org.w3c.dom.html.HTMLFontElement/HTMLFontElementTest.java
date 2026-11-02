/*
 * @test
 * @summary org.w3c.dom.html.HTMLFontElement conformance: HTMLFontElement.
 * @run main HTMLFontElementTest
 */

public class HTMLFontElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLFontElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLFontElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLFontElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLFontElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLFontElement is-a Element");
        check(k.getMethod("getColor") != null, "getColor()");
        check(k.getMethod("getSize") != null, "getSize()");
        check(k.getMethod("setSize", String.class) != null, "setSize(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
