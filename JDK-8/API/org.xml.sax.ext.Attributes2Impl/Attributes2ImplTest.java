/*
 * @test
 * @summary org.xml.sax.ext.Attributes2Impl conformance: attributes2 impl.
 * @run main Attributes2ImplTest
 */


public class Attributes2ImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        org.xml.sax.ext.Attributes2Impl a = new org.xml.sax.ext.Attributes2Impl();
        a.addAttribute("", "id", "id", "ID", "x");
        a.setSpecified(0, false);
        check(!a.isSpecified(0), "not specified");
        a.setDeclared(0, true);
        check(a.isDeclared(0), "declared");
        check(a instanceof org.xml.sax.ext.Attributes2, "is Attributes2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
