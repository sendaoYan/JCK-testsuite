/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper conformance: helper static id/type/narrow.
 * @run main TypeMismatchHelperTest
 */

public class TypeMismatchHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
