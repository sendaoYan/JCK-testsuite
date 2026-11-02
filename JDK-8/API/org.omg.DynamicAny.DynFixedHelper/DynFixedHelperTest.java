/*
 * @test
 * @summary org.omg.DynamicAny.DynFixedHelper conformance: helper static id/type/narrow.
 * @run main DynFixedHelperTest
 */

public class DynFixedHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynFixedHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynFixedHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynFixedHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
