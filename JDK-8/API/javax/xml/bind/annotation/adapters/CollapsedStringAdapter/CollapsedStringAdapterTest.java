/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.CollapsedStringAdapter conformance: collapsed string adapter.
 * @run main CollapsedStringAdapterTest
 */


public class CollapsedStringAdapterTest {
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
        javax.xml.bind.annotation.adapters.CollapsedStringAdapter a = new javax.xml.bind.annotation.adapters.CollapsedStringAdapter();
        check("a b".equals(a.unmarshal("  a   b  ")), "unmarshal collapses whitespace");
        check("a b".equals(a.marshal("a b")), "marshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
