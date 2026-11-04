/*
 * @test
 * @summary org.omg.DynamicAny.DynSequenceHelper conformance: helper static id/type/narrow.
 * @run main DynSequenceHelperTest
 */

public class DynSequenceHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynSequenceHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynSequenceHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynSequenceHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
