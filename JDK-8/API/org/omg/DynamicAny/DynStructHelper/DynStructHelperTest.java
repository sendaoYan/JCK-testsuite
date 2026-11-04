/*
 * @test
 * @summary org.omg.DynamicAny.DynStructHelper conformance: helper static id/type/narrow.
 * @run main DynStructHelperTest
 */

public class DynStructHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynStructHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynStructHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynStructHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
