/*
 * @test
 * @summary javax.xml.ws.soap.AddressingFeature conformance: addressing feature.
 * @run main AddressingFeatureTest
 */


public class AddressingFeatureTest {
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
        javax.xml.ws.soap.AddressingFeature f = new javax.xml.ws.soap.AddressingFeature();
        check("http://www.w3.org/2005/08/addressing/module".equals(f.getID()), "id");
        check(f.isEnabled(), "enabled");
        check(f.getResponses() != null, "responses non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
