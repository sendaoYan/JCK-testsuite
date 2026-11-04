/*
 * @test
 * @summary org.omg.CORBA.AnySeqHolder conformance: holder value field.
 * @run main AnySeqHolderTest
 */

public class AnySeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.AnySeqHolder h0 = new org.omg.CORBA.AnySeqHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Any[] v = null;
        org.omg.CORBA.AnySeqHolder h1 = new org.omg.CORBA.AnySeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
