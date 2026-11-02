/*
 * @test
 * @summary org.w3c.dom.html.HTMLFormElement conformance: HTMLFormElement.
 * @run main HTMLFormElementTest
 */

public class HTMLFormElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLFormElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLFormElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLFormElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLFormElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLFormElement is-a Element");
        check(k.getMethod("getElements") != null, "getElements()");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("setAction", String.class) != null, "setAction(String)");
        check(k.getMethod("submit") != null, "submit()");
        check(k.getMethod("reset") != null, "reset()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
