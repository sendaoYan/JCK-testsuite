/*
 * @test
 * @summary org.omg.CORBA.ULongLongSeqHolder conformance: holder value field.
 * @run main ULongLongSeqHolderTest
 */

public class ULongLongSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ULongLongSeqHolder h0 = new org.omg.CORBA.ULongLongSeqHolder();
        check(h0.value == null, "no-arg default value");
        long[] v = null;
        org.omg.CORBA.ULongLongSeqHolder h1 = new org.omg.CORBA.ULongLongSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
