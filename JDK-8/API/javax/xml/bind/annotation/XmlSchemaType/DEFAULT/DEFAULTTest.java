/*
 * @test
 * @summary javax.xml.bind.annotation.XmlSchemaType.DEFAULT conformance: DEFAULT marker.
 * @run main DEFAULTTest
 */


public class DEFAULTTest {
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
        javax.xml.bind.annotation.XmlSchemaType.DEFAULT d = new javax.xml.bind.annotation.XmlSchemaType.DEFAULT();
        check(d != null, "constructed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
