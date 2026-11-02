/*
 * @test
 * @summary org.w3c.dom.html.HTMLHeadElement conformance: HTMLHeadElement.
 * @run main HTMLHeadElementTest
 */

public class HTMLHeadElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLHeadElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLHeadElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLHeadElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLHeadElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLHeadElement is-a Element");
        check(k.getMethod("getProfile") != null, "getProfile()");
        check(k.getMethod("setProfile", String.class) != null, "setProfile(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
