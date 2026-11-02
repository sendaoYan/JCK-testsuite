/*
 * @test
 * @summary org.omg.CORBA.OctetSeqHolder conformance: holder value field.
 * @run main OctetSeqHolderTest
 */

public class OctetSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.OctetSeqHolder h0 = new org.omg.CORBA.OctetSeqHolder();
        check(h0.value == null, "no-arg default value");
        byte[] v = null;
        org.omg.CORBA.OctetSeqHolder h1 = new org.omg.CORBA.OctetSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
