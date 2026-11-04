/*
 * @test
 * @summary org.omg.CORBA.PolicyListHelper conformance: helper static id/type/narrow.
 * @run main PolicyListHelperTest
 */

public class PolicyListHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyListHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyListHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
