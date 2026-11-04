/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper conformance: helper static id/type/narrow.
 * @run main InconsistentTypeCodeHelperTest
 */

public class InconsistentTypeCodeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
