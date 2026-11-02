/*
 * @test
 * @summary org.w3c.dom.html.HTMLFrameElement conformance: HTMLFrameElement.
 * @run main HTMLFrameElementTest
 */

public class HTMLFrameElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLFrameElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLFrameElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLFrameElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLFrameElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLFrameElement is-a Element");
        check(k.getMethod("getFrameBorder") != null, "getFrameBorder()");
        check(k.getMethod("setSrc", String.class) != null, "setSrc(String)");
        check(k.getMethod("getContentDocument") != null, "getContentDocument()");
        check(k.getMethod("setNoResize", boolean.class) != null, "setNoResize(boolean)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
