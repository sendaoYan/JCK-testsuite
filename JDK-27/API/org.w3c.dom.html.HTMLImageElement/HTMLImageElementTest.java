/*
 * @test
 * @summary org.w3c.dom.html.HTMLImageElement conformance: HTMLImageElement.
 * @run main HTMLImageElementTest
 */

public class HTMLImageElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLImageElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLImageElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLImageElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLImageElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLImageElement is-a Element");
        check(k.getMethod("getLowSrc") != null, "getLowSrc()");
        check(k.getMethod("setSrc", String.class) != null, "setSrc(String)");
        check(k.getMethod("getIsMap") != null, "getIsMap()");
        check(k.getMethod("setIsMap", boolean.class) != null, "setIsMap(boolean)");
        check(k.getMethod("getWidth") != null, "getWidth()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
