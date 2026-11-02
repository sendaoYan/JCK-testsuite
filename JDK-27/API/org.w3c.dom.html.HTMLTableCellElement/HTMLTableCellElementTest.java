/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableCellElement conformance: HTMLTableCellElement.
 * @run main HTMLTableCellElementTest
 */

public class HTMLTableCellElementTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLTableCellElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLTableCellElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLTableCellElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLTableCellElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLTableCellElement is-a Element");
        check(k.getMethod("getCellIndex") != null, "getCellIndex()");
        check(k.getMethod("setColSpan", int.class) != null, "setColSpan(int)");
        check(k.getMethod("getHeaders") != null, "getHeaders()");
        check(k.getMethod("setRowSpan", int.class) != null, "setRowSpan(int)");
        check(k.getMethod("getScope") != null, "getScope()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
