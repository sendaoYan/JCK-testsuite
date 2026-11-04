/*
 * @test
 * @summary org.xml.sax.helpers.LocatorImpl conformance: locator impl.
 * @run main LocatorImplTest
 */


public class LocatorImplTest {
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
        org.xml.sax.helpers.LocatorImpl l = new org.xml.sax.helpers.LocatorImpl();
        l.setLineNumber(5);
        check(l.getLineNumber() == 5, "line");
        l.setColumnNumber(7);
        check(l.getColumnNumber() == 7, "column");
        l.setPublicId("pub");
        check("pub".equals(l.getPublicId()), "publicId");
        l.setSystemId("sys");
        check("sys".equals(l.getSystemId()), "systemId");
        check(l instanceof org.xml.sax.Locator, "is Locator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
