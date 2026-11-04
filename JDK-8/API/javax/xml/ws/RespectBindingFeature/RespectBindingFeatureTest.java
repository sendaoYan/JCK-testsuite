/*
 * @test
 * @summary javax.xml.ws.RespectBindingFeature conformance: respect binding feature.
 * @run main RespectBindingFeatureTest
 */


public class RespectBindingFeatureTest {
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
        javax.xml.ws.RespectBindingFeature f = new javax.xml.ws.RespectBindingFeature();
        check("javax.xml.ws.RespectBindingFeature".equals(f.getID()), "id");
        check(f.isEnabled(), "enabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
