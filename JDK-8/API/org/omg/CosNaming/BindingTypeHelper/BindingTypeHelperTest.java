/*
 * @test
 * @summary org.omg.CosNaming.BindingTypeHelper conformance: helper static id/type/narrow.
 * @run main BindingTypeHelperTest
 */

public class BindingTypeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingTypeHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingTypeHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
