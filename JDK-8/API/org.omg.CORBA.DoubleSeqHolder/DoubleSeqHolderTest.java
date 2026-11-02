/*
 * @test
 * @summary org.omg.CORBA.DoubleSeqHolder conformance: holder value field.
 * @run main DoubleSeqHolderTest
 */

public class DoubleSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.DoubleSeqHolder h0 = new org.omg.CORBA.DoubleSeqHolder();
        check(h0.value == null, "no-arg default value");
        double[] v = null;
        org.omg.CORBA.DoubleSeqHolder h1 = new org.omg.CORBA.DoubleSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
