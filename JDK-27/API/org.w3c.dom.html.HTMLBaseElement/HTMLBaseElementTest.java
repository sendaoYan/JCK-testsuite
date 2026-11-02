/*
 * @test
 * @summary org.w3c.dom.html.HTMLBaseElement conformance: HTMLBaseElement.
 * @run main HTMLBaseElementTest
 */

public class HTMLBaseElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLBaseElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLBaseElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLBaseElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLBaseElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLBaseElement is-a Element");
        check(k.getMethod("getHref") != null, "getHref()");
        check(k.getMethod("setTarget", String.class) != null, "setTarget(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
