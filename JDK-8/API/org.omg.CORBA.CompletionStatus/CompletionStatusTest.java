/*
 * @test
 * @summary org.omg.CORBA.CompletionStatus conformance: enumeration values.
 * @run main CompletionStatusTest
 */

public class CompletionStatusTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.CompletionStatus.COMPLETED_YES != null, "COMPLETED_YES constant non-null");
        check(org.omg.CORBA.CompletionStatus.COMPLETED_YES.value() == org.omg.CORBA.CompletionStatus._COMPLETED_YES, "value() matches int constant");
        check(org.omg.CORBA.CompletionStatus.from_int(org.omg.CORBA.CompletionStatus._COMPLETED_YES) == org.omg.CORBA.CompletionStatus.COMPLETED_YES, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
