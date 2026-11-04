/*
 * @test
 * @summary org.omg.CORBA.ValueMemberHelper conformance: helper static id/type/narrow.
 * @run main ValueMemberHelperTest
 */

public class ValueMemberHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ValueMemberHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ValueMemberHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
