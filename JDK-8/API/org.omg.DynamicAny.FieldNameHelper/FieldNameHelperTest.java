/*
 * @test
 * @summary org.omg.DynamicAny.FieldNameHelper conformance: helper static id/type/narrow.
 * @run main FieldNameHelperTest
 */

public class FieldNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.FieldNameHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.FieldNameHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
