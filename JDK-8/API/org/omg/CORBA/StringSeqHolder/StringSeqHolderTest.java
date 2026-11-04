/*
 * @test
 * @summary org.omg.CORBA.StringSeqHolder conformance: holder value field.
 * @run main StringSeqHolderTest
 */

public class StringSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.StringSeqHolder h0 = new org.omg.CORBA.StringSeqHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.String[] v = null;
        org.omg.CORBA.StringSeqHolder h1 = new org.omg.CORBA.StringSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
