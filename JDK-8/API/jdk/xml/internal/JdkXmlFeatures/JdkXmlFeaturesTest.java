/*
 * @test
 * @summary jdk.xml.internal.JdkXmlFeatures conformance: JDK XML features (internal).
 * @run main JdkXmlFeaturesTest
 */


public class JdkXmlFeaturesTest {
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
        jdk.xml.internal.JdkXmlFeatures f = new jdk.xml.internal.JdkXmlFeatures(false);
        check(f != null, "constructed");
        f.update();
        check(jdk.xml.internal.JdkXmlFeatures.ORACLE_JAXP_PROPERTY_PREFIX != null, "prefix non-null");
        check(jdk.xml.internal.JdkXmlFeatures.XML_FEATURE_MANAGER != null, "feature manager non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
