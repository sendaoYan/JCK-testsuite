/*
 * @test
 * @summary org.w3c.dom.css.DocumentCSS conformance: document CSS.
 * @run main DocumentCSSTest
 */

public class DocumentCSSTest {
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
        Class<?> k = L("org.w3c.dom.css.DocumentCSS");
        check(k.isInterface(), "org.w3c.dom.css.DocumentCSS is an interface");
        check(L("org.w3c.dom.stylesheets.DocumentStyle").isAssignableFrom(k), "DocumentCSS extends DocumentStyle");
        check(k.getInterfaces().length >= 1, "DocumentCSS has superinterface");
        check(k.getMethod("getOverrideStyle", L("org.w3c.dom.Element"), String.class) != null, "getOverrideStyle(org.w3c.dom.Element, String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
