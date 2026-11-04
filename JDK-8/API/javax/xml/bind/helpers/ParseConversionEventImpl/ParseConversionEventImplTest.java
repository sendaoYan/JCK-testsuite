/*
 * @test
 * @summary javax.xml.bind.helpers.ParseConversionEventImpl conformance: parse conversion event impl.
 * @run main ParseConversionEventImplTest
 */


public class ParseConversionEventImplTest {
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
        javax.xml.bind.helpers.ParseConversionEventImpl e = new javax.xml.bind.helpers.ParseConversionEventImpl(1, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(e.getSeverity() == 1, "severity");
        check("msg".equals(e.getMessage()), "message");
        check(e instanceof javax.xml.bind.ParseConversionEvent, "is ParseConversionEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
