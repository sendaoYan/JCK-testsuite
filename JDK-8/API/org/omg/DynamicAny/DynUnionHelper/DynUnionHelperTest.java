/*
 * @test
 * @summary org.omg.DynamicAny.DynUnionHelper conformance: helper static id/type/narrow.
 * @run main DynUnionHelperTest
 */

public class DynUnionHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynUnionHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynUnionHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynUnionHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
