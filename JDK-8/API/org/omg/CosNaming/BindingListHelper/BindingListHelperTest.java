/*
 * @test
 * @summary org.omg.CosNaming.BindingListHelper conformance: helper static id/type/narrow.
 * @run main BindingListHelperTest
 */

public class BindingListHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingListHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingListHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
