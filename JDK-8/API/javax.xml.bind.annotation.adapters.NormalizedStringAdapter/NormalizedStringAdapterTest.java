/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.NormalizedStringAdapter conformance: normalized string adapter.
 * @run main NormalizedStringAdapterTest
 */


public class NormalizedStringAdapterTest {
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
        javax.xml.bind.annotation.adapters.NormalizedStringAdapter a = new javax.xml.bind.annotation.adapters.NormalizedStringAdapter();
        check("a b".equals(a.unmarshal("a	b")), "unmarshal normalizes whitespace");
        check("a b".equals(a.marshal("a b")), "marshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
