/*
 * @test
 * @summary javax.xml.bind.helpers.PrintConversionEventImpl conformance: print conversion event impl.
 * @run main PrintConversionEventImplTest
 */


public class PrintConversionEventImplTest {
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
        javax.xml.bind.helpers.PrintConversionEventImpl e = new javax.xml.bind.helpers.PrintConversionEventImpl(1, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(e.getSeverity() == 1, "severity");
        check("msg".equals(e.getMessage()), "message");
        check(e instanceof javax.xml.bind.PrintConversionEvent, "is PrintConversionEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
