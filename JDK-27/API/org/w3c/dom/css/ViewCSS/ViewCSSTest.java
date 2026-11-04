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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.css.ViewCSS");
        check(k.isInterface(), "org.w3c.dom.css.ViewCSS is an interface");
        check(L("org.w3c.dom.views.AbstractView").isAssignableFrom(k), "ViewCSS extends AbstractView");
        check(k.getInterfaces().length >= 1, "ViewCSS has superinterface");
        check(k.getMethod("getComputedStyle", L("org.w3c.dom.Element"), String.class) != null, "getComputedStyle(org.w3c.dom.Element, String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.ViewCSS a = new org.w3c.dom.css.ViewCSS(){
            public org.w3c.dom.css.CSSStyleDeclaration getComputedStyle(org.w3c.dom.Element p0, java.lang.String p1) { return null; }
            public org.w3c.dom.views.DocumentView getDocument() { return null; }
        };
        check(a != null, "anon impl of ViewCSS");
        check(a.getComputedStyle((org.w3c.dom.Element) null, "s") == null, "getComputedStyle() returns fixed default");
        check(a.getDocument() == null, "getDocument() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
