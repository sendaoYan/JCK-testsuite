/*
 * @test
 * @summary org.omg.CORBA.WrongTransactionHolder conformance: holder value field.
 * @run main WrongTransactionHolderTest
 */

public class WrongTransactionHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.WrongTransactionHolder h0 = new org.omg.CORBA.WrongTransactionHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.WrongTransaction v = null;
        org.omg.CORBA.WrongTransactionHolder h1 = new org.omg.CORBA.WrongTransactionHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
