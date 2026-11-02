/*
 * @test
 * @summary org.w3c.dom.html.HTMLSelectElement conformance: HTMLSelectElement.
 * @run main HTMLSelectElementTest
 */

public class HTMLSelectElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLSelectElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLSelectElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLSelectElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLSelectElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLSelectElement is-a Element");
        check(k.getMethod("getType") != null, "getType()");
        check(k.getMethod("getSelectedIndex") != null, "getSelectedIndex()");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("getOptions") != null, "getOptions()");
        check(k.getMethod("setMultiple", boolean.class) != null, "setMultiple(boolean)");
        check(k.getMethod("add", L("org.w3c.dom.html.HTMLElement"), L("org.w3c.dom.html.HTMLElement")) != null, "add(org.w3c.dom.html.HTMLElement, org.w3c.dom.html.HTMLElement)");
        check(k.getMethod("remove", int.class) != null, "remove(int)");
        check(k.getMethod("focus") != null, "focus()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
