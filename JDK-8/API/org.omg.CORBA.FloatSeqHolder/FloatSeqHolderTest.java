/*
 * @test
 * @summary org.omg.CORBA.FloatSeqHolder conformance: holder value field.
 * @run main FloatSeqHolderTest
 */

public class FloatSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.FloatSeqHolder h0 = new org.omg.CORBA.FloatSeqHolder();
        check(h0.value == null, "no-arg default value");
        float[] v = null;
        org.omg.CORBA.FloatSeqHolder h1 = new org.omg.CORBA.FloatSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
