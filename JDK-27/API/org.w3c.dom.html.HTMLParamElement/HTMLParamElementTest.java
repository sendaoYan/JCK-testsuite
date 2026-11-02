/*
 * @test
 * @summary org.w3c.dom.html.HTMLParamElement conformance: HTMLParamElement.
 * @run main HTMLParamElementTest
 */

public class HTMLParamElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLParamElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLParamElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLParamElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLParamElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLParamElement is-a Element");
        check(k.getMethod("getName") != null, "getName()");
        check(k.getMethod("setValue", String.class) != null, "setValue(String)");
        check(k.getMethod("getValueType") != null, "getValueType()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
