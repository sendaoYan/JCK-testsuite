/*
 * @test
 * @summary javax.xml.bind.helpers.ValidationEventLocatorImpl conformance: validation event locator impl.
 * @run main ValidationEventLocatorImplTest
 */


public class ValidationEventLocatorImplTest {
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
        javax.xml.bind.helpers.ValidationEventLocatorImpl l = new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        l.setLineNumber(10);
        check(l.getLineNumber() == 10, "line number");
        l.setColumnNumber(20);
        check(l.getColumnNumber() == 20, "column number");
        l.setOffset(30);
        check(l.getOffset() == 30, "offset");
        l.setURL(new java.net.URL("http://example.com/x"));
        check(l.getURL() != null, "url");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
