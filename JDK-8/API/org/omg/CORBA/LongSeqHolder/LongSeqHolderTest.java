/*
 * @test
 * @summary org.omg.CORBA.LongSeqHolder conformance: holder value field.
 * @run main LongSeqHolderTest
 */

public class LongSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LongSeqHolder h0 = new org.omg.CORBA.LongSeqHolder();
        check(h0.value == null, "no-arg default value");
        int[] v = null;
        org.omg.CORBA.LongSeqHolder h1 = new org.omg.CORBA.LongSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
