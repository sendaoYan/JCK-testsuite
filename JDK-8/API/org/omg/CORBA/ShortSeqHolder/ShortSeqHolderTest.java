/*
 * @test
 * @summary org.omg.CORBA.ShortSeqHolder conformance: holder value field.
 * @run main ShortSeqHolderTest
 */

public class ShortSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ShortSeqHolder h0 = new org.omg.CORBA.ShortSeqHolder();
        check(h0.value == null, "no-arg default value");
        short[] v = null;
        org.omg.CORBA.ShortSeqHolder h1 = new org.omg.CORBA.ShortSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
