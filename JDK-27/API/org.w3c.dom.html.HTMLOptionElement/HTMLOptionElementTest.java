/*
 * @test
 * @summary org.w3c.dom.html.HTMLOptionElement conformance: HTMLOptionElement.
 * @run main HTMLOptionElementTest
 */

public class HTMLOptionElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLOptionElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLOptionElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLOptionElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLOptionElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLOptionElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("getDefaultSelected") != null, "getDefaultSelected()");
        check(k.getMethod("getIndex") != null, "getIndex()");
        check(k.getMethod("setSelected", boolean.class) != null, "setSelected(boolean)");
        check(k.getMethod("getValue") != null, "getValue()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
