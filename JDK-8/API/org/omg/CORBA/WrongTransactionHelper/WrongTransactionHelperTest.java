/*
 * @test
 * @summary org.omg.CORBA.WrongTransactionHelper conformance: helper static id/type/narrow.
 * @run main WrongTransactionHelperTest
 */

public class WrongTransactionHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.WrongTransactionHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.WrongTransactionHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
