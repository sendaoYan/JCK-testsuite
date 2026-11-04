/*
 * @test
 * @summary javax.xml.bind.helpers.ValidationEventImpl conformance: validation event impl.
 * @run main ValidationEventImplTest
 */


public class ValidationEventImplTest {
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
        javax.xml.bind.helpers.ValidationEventImpl e = new javax.xml.bind.helpers.ValidationEventImpl(2, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(e.getSeverity() == 2, "severity");
        check("msg".equals(e.getMessage()), "message");
        e.setSeverity(1);
        check(e.getSeverity() == 1, "set severity");
        e.setMessage("x");
        check("x".equals(e.getMessage()), "set message");
        java.lang.Throwable t = new java.lang.Exception("cause");
        e.setLinkedException(t);
        check(e.getLinkedException() == t, "linked exception");
        check(e.getLocator() == null, "locator null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
