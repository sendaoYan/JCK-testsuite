/*
 * @test
 * @summary javax.xml.ws.Holder conformance: holder value wrapper.
 * @run main HolderTest
 */


public class HolderTest {
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
        javax.xml.ws.Holder<String> h = new javax.xml.ws.Holder<String>("x");
        check("x".equals(h.value), "value field");
        h.value = "y";
        check("y".equals(h.value), "set value");
        check(new javax.xml.ws.Holder<String>().value == null, "default null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
