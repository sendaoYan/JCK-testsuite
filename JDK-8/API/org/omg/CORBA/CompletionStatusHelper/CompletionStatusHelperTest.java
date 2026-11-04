/*
 * @test
 * @summary org.omg.CORBA.CompletionStatusHelper conformance: helper static id/type/narrow.
 * @run main CompletionStatusHelperTest
 */

public class CompletionStatusHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.CompletionStatusHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.CompletionStatusHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
