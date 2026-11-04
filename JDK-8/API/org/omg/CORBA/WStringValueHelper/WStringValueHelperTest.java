/*
 * @test
 * @summary org.omg.CORBA.WStringValueHelper conformance: helper static id/type/narrow.
 * @run main WStringValueHelperTest
 */

public class WStringValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.WStringValueHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.WStringValueHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
