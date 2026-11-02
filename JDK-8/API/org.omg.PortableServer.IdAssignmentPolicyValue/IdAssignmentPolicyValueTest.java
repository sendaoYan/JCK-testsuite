/*
 * @test
 * @summary org.omg.PortableServer.IdAssignmentPolicyValue conformance: enumeration values.
 * @run main IdAssignmentPolicyValueTest
 */

public class IdAssignmentPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.IdAssignmentPolicyValue.USER_ID != null, "USER_ID constant non-null");
        check(org.omg.PortableServer.IdAssignmentPolicyValue.USER_ID.value() == org.omg.PortableServer.IdAssignmentPolicyValue._USER_ID, "value() matches int constant");
        check(org.omg.PortableServer.IdAssignmentPolicyValue.from_int(org.omg.PortableServer.IdAssignmentPolicyValue._USER_ID) == org.omg.PortableServer.IdAssignmentPolicyValue.USER_ID, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
