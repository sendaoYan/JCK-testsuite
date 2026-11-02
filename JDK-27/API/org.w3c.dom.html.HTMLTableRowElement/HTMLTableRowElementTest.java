/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableRowElement conformance: HTMLTableRowElement.
 * @run main HTMLTableRowElementTest
 */

public class HTMLTableRowElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLTableRowElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLTableRowElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLTableRowElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLTableRowElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLTableRowElement is-a Element");
        check(k.getMethod("getRowIndex") != null, "getRowIndex()");
        check(k.getMethod("getSectionRowIndex") != null, "getSectionRowIndex()");
        check(k.getMethod("getCells") != null, "getCells()");
        check(k.getMethod("insertCell", int.class) != null, "insertCell(int)");
        check(k.getMethod("deleteCell", int.class) != null, "deleteCell(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
