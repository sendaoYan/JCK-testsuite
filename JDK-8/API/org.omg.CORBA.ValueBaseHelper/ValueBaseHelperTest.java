/*
 * @test
 * @summary org.omg.CORBA.ValueBaseHelper conformance: helper static id/type/narrow.
 * @run main ValueBaseHelperTest
 */

public class ValueBaseHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ValueBaseHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ValueBaseHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
