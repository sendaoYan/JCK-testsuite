/*
 * @test
 * @summary javax.xml.bind.annotation.W3CDomHandler conformance: W3C DOM handler.
 * @run main W3CDomHandlerTest
 */


public class W3CDomHandlerTest {
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
        javax.xml.bind.annotation.W3CDomHandler h = new javax.xml.bind.annotation.W3CDomHandler();
        check(h.getBuilder() == null, "default builder null");
        javax.xml.transform.dom.DOMResult r = h.createUnmarshaller(null);
        check(r != null, "createUnmarshaller non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
