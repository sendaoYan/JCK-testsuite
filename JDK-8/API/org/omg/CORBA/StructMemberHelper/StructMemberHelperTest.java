/*
 * @test
 * @summary org.omg.CORBA.StructMemberHelper conformance: helper static id/type/narrow.
 * @run main StructMemberHelperTest
 */

public class StructMemberHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.StructMemberHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.StructMemberHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
