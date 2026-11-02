/*
 * @test
 * @summary org.w3c.dom.css.ViewCSS conformance: computed style view.
 * @run main ViewCSSTest
 */

public class ViewCSSTest {
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
        Class<?> k = L("org.w3c.dom.css.ViewCSS");
        check(k.isInterface(), "org.w3c.dom.css.ViewCSS is an interface");
        check(L("org.w3c.dom.views.AbstractView").isAssignableFrom(k), "ViewCSS extends AbstractView");
        check(k.getInterfaces().length >= 1, "ViewCSS has superinterface");
        check(k.getMethod("getComputedStyle", L("org.w3c.dom.Element"), String.class) != null, "getComputedStyle(org.w3c.dom.Element, String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
