/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementDecl.GLOBAL conformance: GLOBAL marker.
 * @run main GLOBALTest
 */


public class GLOBALTest {
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
        javax.xml.bind.annotation.XmlElementDecl.GLOBAL g = new javax.xml.bind.annotation.XmlElementDecl.GLOBAL();
        check(g != null, "constructed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
