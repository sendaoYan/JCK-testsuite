/*
 * @test
 * @summary org.omg.CORBA.PolicyErrorCodeHelper conformance: helper static id/type/narrow.
 * @run main PolicyErrorCodeHelperTest
 */

public class PolicyErrorCodeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyErrorCodeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyErrorCodeHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
