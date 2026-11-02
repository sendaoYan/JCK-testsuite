/*
 * @test
 * @summary org.omg.CORBA.PolicyHelper conformance: helper static id/type/narrow.
 * @run main PolicyHelperTest
 */

public class PolicyHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyHelper.type() != null, "type() non-null");
        check(org.omg.CORBA.PolicyHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
