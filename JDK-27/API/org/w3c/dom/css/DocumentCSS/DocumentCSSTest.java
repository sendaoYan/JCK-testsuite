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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.css.DocumentCSS");
        check(k.isInterface(), "org.w3c.dom.css.DocumentCSS is an interface");
        check(L("org.w3c.dom.stylesheets.DocumentStyle").isAssignableFrom(k), "DocumentCSS extends DocumentStyle");
        check(k.getInterfaces().length >= 1, "DocumentCSS has superinterface");
        check(k.getMethod("getOverrideStyle", L("org.w3c.dom.Element"), String.class) != null, "getOverrideStyle(org.w3c.dom.Element, String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.DocumentCSS a = new org.w3c.dom.css.DocumentCSS(){
            public org.w3c.dom.css.CSSStyleDeclaration getOverrideStyle(org.w3c.dom.Element p0, java.lang.String p1) { return null; }
            public org.w3c.dom.stylesheets.StyleSheetList getStyleSheets() { return null; }
        };
        check(a != null, "anon impl of DocumentCSS");
        check(a.getOverrideStyle((org.w3c.dom.Element) null, "s") == null, "getOverrideStyle() returns fixed default");
        check(a.getStyleSheets() == null, "getStyleSheets() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
