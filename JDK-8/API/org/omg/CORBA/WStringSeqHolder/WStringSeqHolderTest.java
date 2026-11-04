/*
 * @test
 * @summary org.omg.CORBA.WStringSeqHolder conformance: holder value field.
 * @run main WStringSeqHolderTest
 */

public class WStringSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.WStringSeqHolder h0 = new org.omg.CORBA.WStringSeqHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.String[] v = null;
        org.omg.CORBA.WStringSeqHolder h1 = new org.omg.CORBA.WStringSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
