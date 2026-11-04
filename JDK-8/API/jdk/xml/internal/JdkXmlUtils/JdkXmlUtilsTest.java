/*
 * @test
 * @summary jdk.xml.internal.JdkXmlUtils conformance: JDK XML utils (internal).
 * @run main JdkXmlUtilsTest
 */


public class JdkXmlUtilsTest {
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
        check("true".equals(jdk.xml.internal.JdkXmlUtils.FEATURE_TRUE), "FEATURE_TRUE");
        check("false".equals(jdk.xml.internal.JdkXmlUtils.FEATURE_FALSE), "FEATURE_FALSE");
        check(jdk.xml.internal.JdkXmlUtils.getValue(5, 3) == 5, "getValue");
        check(jdk.xml.internal.JdkXmlUtils.getDOMFactory(false) != null, "getDOMFactory non-null");
        check(jdk.xml.internal.JdkXmlUtils.getDOMDocument() != null, "getDOMDocument non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
