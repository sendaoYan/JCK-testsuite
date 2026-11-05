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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LocatorImpl contract ----
    static void deepContract() throws Exception {
        org.xml.sax.helpers.LocatorImpl l = new org.xml.sax.helpers.LocatorImpl();
        check(l.getLineNumber() == 0 && l.getColumnNumber() == 0, "fresh: position 0/0 (JDK impl default, not the documented -1)");
        check(l.getPublicId() == null && l.getSystemId() == null, "fresh: ids null");
        l.setPublicId("pub"); l.setSystemId("sys");
        l.setLineNumber(7); l.setColumnNumber(9);
        check("pub".equals(l.getPublicId()) && "sys".equals(l.getSystemId()), "id setters round-trip");
        check(l.getLineNumber() == 7 && l.getColumnNumber() == 9, "position setters round-trip");
        org.xml.sax.helpers.LocatorImpl copy = new org.xml.sax.helpers.LocatorImpl(l);
        check(copy.getLineNumber() == 7 && copy.getColumnNumber() == 9, "copy ctor carries position");
        check("pub".equals(copy.getPublicId()) && "sys".equals(copy.getSystemId()), "copy ctor carries ids");
        l.setLineNumber(1);
        check(copy.getLineNumber() == 7, "copy is a snapshot, not a live view");
        // supplies Locator where a parser hands one out
        org.xml.sax.Locator base = l;
        check(base instanceof org.xml.sax.Locator, "LocatorImpl is a Locator");
    }
}
