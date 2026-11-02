/*
 * @test
 * @summary javax.xml.bind.JAXBElement.GlobalScope conformance: global scope marker.
 * @run main GlobalScopeTest
 */


public class GlobalScopeTest {
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
        javax.xml.bind.JAXBElement.GlobalScope gs = new javax.xml.bind.JAXBElement.GlobalScope();
        check(gs != null, "constructed");
        check(gs.getClass() == javax.xml.bind.JAXBElement.GlobalScope.class, "type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
