/*
 * @test
 * @summary org.omg.DynamicAny.AnySeqHelper conformance: helper static id/type/narrow.
 * @run main AnySeqHelperTest
 */

public class AnySeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.AnySeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.AnySeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
