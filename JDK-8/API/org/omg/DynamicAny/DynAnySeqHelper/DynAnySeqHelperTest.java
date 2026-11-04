/*
 * @test
 * @summary org.omg.DynamicAny.DynAnySeqHelper conformance: helper static id/type/narrow.
 * @run main DynAnySeqHelperTest
 */

public class DynAnySeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnySeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnySeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
