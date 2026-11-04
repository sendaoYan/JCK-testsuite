/*
 * @test
 * @summary org.omg.CosNaming.BindingIteratorHelper conformance: helper static id/type/narrow.
 * @run main BindingIteratorHelperTest
 */

public class BindingIteratorHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingIteratorHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingIteratorHelper.type() != null, "type() non-null");
        check(org.omg.CosNaming.BindingIteratorHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
