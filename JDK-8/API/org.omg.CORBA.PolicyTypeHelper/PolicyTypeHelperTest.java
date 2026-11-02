/*
 * @test
 * @summary org.omg.CORBA.PolicyTypeHelper conformance: helper static id/type/narrow.
 * @run main PolicyTypeHelperTest
 */

public class PolicyTypeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyTypeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyTypeHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
