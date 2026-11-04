/*
 * @test
 * @summary java.security.spec.ECFieldF2m conformance: constructor and getters.
 * @run main ECFieldF2mTest
 */

public class ECFieldF2mTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.ECFieldF2m f = new java.security.spec.ECFieldF2m(113);
        check(f.getM() == 113, "getM");
        check(f.getFieldSize() == 113, "getFieldSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
