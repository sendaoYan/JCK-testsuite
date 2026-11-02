/*
 * @test
 * @summary org.w3c.dom.html.HTMLHRElement conformance: HTMLHRElement.
 * @run main HTMLHRElementTest
 */

public class HTMLHRElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLHRElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLHRElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLHRElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLHRElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLHRElement is-a Element");
        check(k.getMethod("getAlign") != null, "getAlign()");
        check(k.getMethod("setNoShade", boolean.class) != null, "setNoShade(boolean)");
        check(k.getMethod("getSize") != null, "getSize()");
        check(k.getMethod("getWidth") != null, "getWidth()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
