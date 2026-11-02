/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager conformance: XML security manager (internal).
 * @run main XMLSecurityManagerTest
 */


public class XMLSecurityManagerTest {
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
        jdk.xml.internal.XMLSecurityManager m = new jdk.xml.internal.XMLSecurityManager();
        check(m != null, "constructed");
        m.setSecureProcessing(true);
        check(m.isSecureProcessing(), "secure processing");
        check(m.getLimit(jdk.xml.internal.XMLSecurityManager.Limit.ENTITY_EXPANSION_LIMIT) >= 0, "limit non-negative");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
