/*
 * @test
 * @summary org.omg.CORBA.WCharSeqHolder conformance: holder value field.
 * @run main WCharSeqHolderTest
 */

public class WCharSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.WCharSeqHolder h0 = new org.omg.CORBA.WCharSeqHolder();
        check(h0.value == null, "no-arg default value");
        char[] v = null;
        org.omg.CORBA.WCharSeqHolder h1 = new org.omg.CORBA.WCharSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
