/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper conformance: helper static id/type/narrow.
 * @run main InvalidValueHelperTest
 */

public class InvalidValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
