/*
 * @test
 * @summary org.w3c.dom.html.HTMLButtonElement conformance: HTMLButtonElement.
 * @run main HTMLButtonElementTest
 */

public class HTMLButtonElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLButtonElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLButtonElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLButtonElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLButtonElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLButtonElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("setDisabled", boolean.class) != null, "setDisabled(boolean)");
        check(k.getMethod("getValue") != null, "getValue()");
        check(k.getMethod("setValue", String.class) != null, "setValue(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
