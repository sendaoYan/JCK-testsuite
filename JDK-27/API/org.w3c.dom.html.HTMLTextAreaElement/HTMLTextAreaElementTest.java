/*
 * @test
 * @summary org.w3c.dom.html.HTMLTextAreaElement conformance: HTMLTextAreaElement.
 * @run main HTMLTextAreaElementTest
 */

public class HTMLTextAreaElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLTextAreaElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLTextAreaElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLTextAreaElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLTextAreaElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLTextAreaElement is-a Element");
        check(k.getMethod("getDefaultValue") != null, "getDefaultValue()");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("setCols", int.class) != null, "setCols(int)");
        check(k.getMethod("getType") != null, "getType()");
        check(k.getMethod("getValue") != null, "getValue()");
        check(k.getMethod("select") != null, "select()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
