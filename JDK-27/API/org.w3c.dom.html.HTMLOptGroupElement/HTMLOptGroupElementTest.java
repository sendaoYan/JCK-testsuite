/*
 * @test
 * @summary org.w3c.dom.html.HTMLOptGroupElement conformance: HTMLOptGroupElement.
 * @run main HTMLOptGroupElementTest
 */

public class HTMLOptGroupElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLOptGroupElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLOptGroupElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLOptGroupElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLOptGroupElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLOptGroupElement is-a Element");
        check(k.getMethod("getDisabled") != null, "getDisabled()");
        check(k.getMethod("setLabel", String.class) != null, "setLabel(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
