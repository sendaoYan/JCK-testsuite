/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyHelper conformance: helper static id/type/narrow.
 * @run main DynAnyHelperTest
 */

public class DynAnyHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynAnyHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
