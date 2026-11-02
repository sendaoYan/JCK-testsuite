/*
 * @test
 * @summary org.w3c.dom.html.HTMLMapElement conformance: HTMLMapElement.
 * @run main HTMLMapElementTest
 */

public class HTMLMapElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLMapElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLMapElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLMapElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLMapElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLMapElement is-a Element");
        check(k.getMethod("getAreas") != null, "getAreas()");
        check(k.getMethod("setName", String.class) != null, "setName(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
