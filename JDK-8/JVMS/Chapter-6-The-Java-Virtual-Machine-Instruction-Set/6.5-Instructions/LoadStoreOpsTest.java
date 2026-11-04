/*
 * @test
 * @summary JVMS 6.5: load/store instructions (iconst_*, aconst_null, lconst_*,
 *          fconst_*, dconst_*, iload/lload/fload/dload/aload and the _N and
 *          wide forms, istore/lstore/fstore/dstore/astore) plus ldc_w and the
 *          bipush/sipush boundary values.
 * @compile LoadStoreOps.jasm
 * @run main LoadStoreOpsTest
 */

public class LoadStoreOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(LoadStoreOps.iconstM1() == -1, "iconst_m1");
        check(LoadStoreOps.iconstSum() == 15, "iconst_0..5 sum");
        check(LoadStoreOps.aconstNull() == null, "aconst_null");
        check(LoadStoreOps.lconst0() == 0L, "lconst_0");
        check(LoadStoreOps.lconst1() == 1L, "lconst_1");
        check(LoadStoreOps.fconstSum() == 3.0f, "fconst_0..2 sum");
        check(LoadStoreOps.dconstSum() == 1.0, "dconst_0/1 sum");

        check(LoadStoreOps.iloadStore(42) == 42, "iload_0/istore_1/iload_1");
        check(LoadStoreOps.lloadStore(1234567890123L) == 1234567890123L, "lload/lstore");
        check(LoadStoreOps.floadStore(1.5f) == 1.5f, "fload/fstore");
        check(LoadStoreOps.dloadStore(2.5) == 2.5, "dload/dstore");
        String s = "x";
        check(LoadStoreOps.aloadStore(s) == s, "aload/astore");

        check(LoadStoreOps.wideStoreLoad(77) == 77, "wide iload/istore index 300");
        check(LoadStoreOps.ldcW() == 40000, "ldc_w");
        check(LoadStoreOps.bipushMin() == -128, "bipush -128");
        check(LoadStoreOps.bipushMax() == 127, "bipush 127");
        check(LoadStoreOps.sipushMin() == -32768, "sipush -32768");
        check(LoadStoreOps.sipushMax() == 32767, "sipush 32767");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
