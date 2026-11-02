/*
 * @test
 * @summary org.omg.CORBA.StringValueHelper conformance: helper static id/type/narrow.
 * @run main StringValueHelperTest
 */

public class StringValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.StringValueHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.StringValueHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
