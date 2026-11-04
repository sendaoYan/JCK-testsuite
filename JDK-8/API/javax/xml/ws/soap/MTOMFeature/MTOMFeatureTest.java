/*
 * @test
 * @summary javax.xml.ws.soap.MTOMFeature conformance: MTOM feature.
 * @run main MTOMFeatureTest
 */


public class MTOMFeatureTest {
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
        javax.xml.ws.soap.MTOMFeature f = new javax.xml.ws.soap.MTOMFeature();
        check("http://www.w3.org/2004/08/soap/features/http-optimization".equals(f.getID()), "id");
        check(f.isEnabled(), "enabled");
        check(f.getThreshold() == 0, "default threshold");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
