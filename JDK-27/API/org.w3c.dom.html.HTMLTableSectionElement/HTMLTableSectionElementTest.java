/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableSectionElement conformance: HTMLTableSectionElement.
 * @run main HTMLTableSectionElementTest
 */

public class HTMLTableSectionElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLTableSectionElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLTableSectionElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLTableSectionElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLTableSectionElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLTableSectionElement is-a Element");
        check(k.getMethod("getAlign") != null, "getAlign()");
        check(k.getMethod("getRows") != null, "getRows()");
        check(k.getMethod("insertRow", int.class) != null, "insertRow(int)");
        check(k.getMethod("deleteRow", int.class) != null, "deleteRow(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
