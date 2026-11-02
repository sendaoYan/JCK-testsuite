/*
 * @test
 * @summary org.w3c.dom.html.HTMLIsIndexElement conformance: HTMLIsIndexElement.
 * @run main HTMLIsIndexElementTest
 */

public class HTMLIsIndexElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLIsIndexElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLIsIndexElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLIsIndexElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLIsIndexElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLIsIndexElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("getPrompt") != null, "getPrompt()");
        check(k.getMethod("setPrompt", String.class) != null, "setPrompt(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
