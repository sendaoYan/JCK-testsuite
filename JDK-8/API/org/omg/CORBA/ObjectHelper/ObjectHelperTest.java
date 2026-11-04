/*
 * @test
 * @summary org.omg.CORBA.ObjectHelper conformance: helper static id/type/narrow.
 * @run main ObjectHelperTest
 */

public class ObjectHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ObjectHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ObjectHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
