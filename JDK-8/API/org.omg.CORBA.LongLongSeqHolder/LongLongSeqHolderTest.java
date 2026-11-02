/*
 * @test
 * @summary org.omg.CORBA.LongLongSeqHolder conformance: holder value field.
 * @run main LongLongSeqHolderTest
 */

public class LongLongSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LongLongSeqHolder h0 = new org.omg.CORBA.LongLongSeqHolder();
        check(h0.value == null, "no-arg default value");
        long[] v = null;
        org.omg.CORBA.LongLongSeqHolder h1 = new org.omg.CORBA.LongLongSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
