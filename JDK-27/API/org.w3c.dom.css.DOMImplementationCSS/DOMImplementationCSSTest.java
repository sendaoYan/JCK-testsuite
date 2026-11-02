/*
 * @test
 * @summary org.w3c.dom.css.DOMImplementationCSS conformance: DOMImplementation subtype.
 * @run main DOMImplementationCSSTest
 */

public class DOMImplementationCSSTest {
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
        Class<?> k = L("org.w3c.dom.css.DOMImplementationCSS");
        check(k.isInterface(), "org.w3c.dom.css.DOMImplementationCSS is an interface");
        check(L("org.w3c.dom.DOMImplementation").isAssignableFrom(k), "DOMImplementationCSS extends DOMImplementation");
        check(k.getInterfaces().length >= 1, "DOMImplementationCSS has superinterface");
        check(k.getMethod("createCSSStyleSheet", String.class, String.class) != null, "createCSSStyleSheet(String, String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
