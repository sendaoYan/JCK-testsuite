/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableElement conformance: HTMLTableElement.
 * @run main HTMLTableElementTest
 */

public class HTMLTableElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLTableElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLTableElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLTableElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLTableElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLTableElement is-a Element");
        check(k.getMethod("getCaption") != null, "getCaption()");
        check(k.getMethod("getRows") != null, "getRows()");
        check(k.getMethod("getTBodies") != null, "getTBodies()");
        check(k.getMethod("setBorder", String.class) != null, "setBorder(String)");
        check(k.getMethod("createTHead") != null, "createTHead()");
        check(k.getMethod("insertRow", int.class) != null, "insertRow(int)");
        check(k.getMethod("deleteRow", int.class) != null, "deleteRow(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
