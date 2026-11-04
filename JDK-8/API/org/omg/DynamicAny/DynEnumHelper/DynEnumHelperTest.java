/*
 * @test
 * @summary org.omg.DynamicAny.DynEnumHelper conformance: helper static id/type/narrow.
 * @run main DynEnumHelperTest
 */

public class DynEnumHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynEnumHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynEnumHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynEnumHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
