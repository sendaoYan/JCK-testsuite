/*
 * @test
 * @summary org.w3c.dom.html.HTMLScriptElement conformance: HTMLScriptElement.
 * @run main HTMLScriptElementTest
 */

public class HTMLScriptElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLScriptElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLScriptElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLScriptElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLScriptElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLScriptElement is-a Element");
        check(k.getMethod("getText") != null, "getText()");
        check(k.getMethod("setSrc", String.class) != null, "setSrc(String)");
        check(k.getMethod("getDefer") != null, "getDefer()");
        check(k.getMethod("getEvent") != null, "getEvent()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
