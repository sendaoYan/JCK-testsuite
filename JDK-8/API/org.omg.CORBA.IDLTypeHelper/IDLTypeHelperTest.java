/*
 * @test
 * @summary org.omg.CORBA.IDLTypeHelper conformance: helper static id/type/narrow.
 * @run main IDLTypeHelperTest
 */

public class IDLTypeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.IDLTypeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.IDLTypeHelper.type() != null, "type() non-null");
        check(org.omg.CORBA.IDLTypeHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
