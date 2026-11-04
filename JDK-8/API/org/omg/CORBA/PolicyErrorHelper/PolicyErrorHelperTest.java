/*
 * @test
 * @summary org.omg.CORBA.PolicyErrorHelper conformance: helper static id/type/narrow.
 * @run main PolicyErrorHelperTest
 */

public class PolicyErrorHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyErrorHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyErrorHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
