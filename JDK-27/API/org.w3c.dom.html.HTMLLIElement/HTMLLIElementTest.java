/*
 * @test
 * @summary org.w3c.dom.html.HTMLLIElement conformance: HTMLLIElement.
 * @run main HTMLLIElementTest
 */

public class HTMLLIElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLLIElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLLIElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLLIElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLLIElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLLIElement is-a Element");
        check(k.getMethod("getType") != null, "getType()");
        check(k.getMethod("setValue", int.class) != null, "setValue(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
