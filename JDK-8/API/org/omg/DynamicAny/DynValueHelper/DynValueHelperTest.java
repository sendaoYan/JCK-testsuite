/*
 * @test
 * @summary org.omg.DynamicAny.DynValueHelper conformance: helper static id/type/narrow.
 * @run main DynValueHelperTest
 */

public class DynValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynValueHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynValueHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynValueHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
