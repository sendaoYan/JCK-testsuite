/*
 * @test
 * @summary jdk.xml.internal.XMLLimitAnalyzer conformance: XML limit analyzer (internal).
 * @run main XMLLimitAnalyzerTest
 */


public class XMLLimitAnalyzerTest {
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
        jdk.xml.internal.XMLLimitAnalyzer a = new jdk.xml.internal.XMLLimitAnalyzer();
        check(a != null, "constructed");
        check(!a.isTracking("unknown"), "not tracking unknown");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
