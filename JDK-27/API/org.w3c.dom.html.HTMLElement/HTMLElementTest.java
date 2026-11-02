/*
 * @test
 * @summary org.w3c.dom.html.HTMLElement conformance: Element subtype.
 * @run main HTMLElementTest
 */

public class HTMLElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLElement is an interface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLElement extends Element");
        check(k.getInterfaces().length >= 1, "HTMLElement has superinterface");
        check(k.getMethod("getId") != null, "getId()");
        check(k.getMethod("setId", String.class) != null, "setId(String)");
        check(k.getMethod("getTitle") != null, "getTitle()");
        check(k.getMethod("getClassName") != null, "getClassName()");
        check(k.getMethod("setClassName", String.class) != null, "setClassName(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
