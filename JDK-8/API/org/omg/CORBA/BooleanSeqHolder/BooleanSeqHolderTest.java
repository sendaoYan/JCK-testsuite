/*
 * @test
 * @summary org.omg.CORBA.BooleanSeqHolder conformance: holder value field.
 * @run main BooleanSeqHolderTest
 */

public class BooleanSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.BooleanSeqHolder h0 = new org.omg.CORBA.BooleanSeqHolder();
        check(h0.value == null, "no-arg default value");
        boolean[] v = null;
        org.omg.CORBA.BooleanSeqHolder h1 = new org.omg.CORBA.BooleanSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
