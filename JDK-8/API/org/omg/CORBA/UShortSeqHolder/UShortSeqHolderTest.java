/*
 * @test
 * @summary org.omg.CORBA.UShortSeqHolder conformance: holder value field.
 * @run main UShortSeqHolderTest
 */

public class UShortSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.UShortSeqHolder h0 = new org.omg.CORBA.UShortSeqHolder();
        check(h0.value == null, "no-arg default value");
        short[] v = null;
        org.omg.CORBA.UShortSeqHolder h1 = new org.omg.CORBA.UShortSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
