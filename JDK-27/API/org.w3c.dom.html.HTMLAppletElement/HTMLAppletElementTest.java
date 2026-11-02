/*
 * @test
 * @summary org.w3c.dom.html.HTMLAppletElement conformance: HTMLAppletElement.
 * @run main HTMLAppletElementTest
 */

public class HTMLAppletElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLAppletElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLAppletElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLAppletElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLAppletElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLAppletElement is-a Element");
        check(k.getMethod("getCode") != null, "getCode()");
        check(k.getMethod("setCode", String.class) != null, "setCode(String)");
        check(k.getMethod("getCodeBase") != null, "getCodeBase()");
        check(k.getMethod("getObject") != null, "getObject()");
        check(k.getMethod("getWidth") != null, "getWidth()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
