/*
 * @test
 * @summary javax.xml.soap.SAAJResult conformance: SAAJ result.
 * @run main SAAJResultTest
 */


public class SAAJResultTest {
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
        javax.xml.soap.SAAJResult r = new javax.xml.soap.SAAJResult();
        check(r.getResult() == null, "result null");
        check(r instanceof javax.xml.transform.dom.DOMResult, "extends DOMResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
