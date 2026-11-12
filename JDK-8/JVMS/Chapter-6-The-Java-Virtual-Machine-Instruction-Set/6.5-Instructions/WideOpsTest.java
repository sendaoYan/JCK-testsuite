/*
 * @test
 * @summary JVMS 6.5 "wide": the wide prefix extends iload/lload/fload/dload/
 *          aload, istore/lstore/fstore/dstore/astore and iinc to a 16-bit
 *          index (and iinc to a 16-bit constant). Local index 256 is only
 *          reachable through the wide encoding, so a correct round-trip
 *          through local 256 proves wide was emitted and executed.
 * @compile WideOps.jasm
 * @run main WideOpsTest
 */

public class WideOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(WideOps.wideInt(1234) == 1234, "wide int store/load index 256");
        check(WideOps.wideLong(123456789012345L) == 123456789012345L, "wide long store/load index 256");
        check(WideOps.wideFloat(3.5f) == 3.5f, "wide float store/load index 256");
        check(WideOps.wideDouble(-0.25d) == -0.25d, "wide double store/load index 256");
        Object o = new Object();
        check(WideOps.wideRef(o) == o, "wide ref store/load index 256");
        check(WideOps.wideIincIndex(10) == 15, "wide iinc index 256 by +5");
        check(WideOps.wideIincConst() == 300, "wide iinc constant 300 (outside s1)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
