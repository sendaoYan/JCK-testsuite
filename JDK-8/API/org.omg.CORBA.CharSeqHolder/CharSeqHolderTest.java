/*
 * @test
 * @summary org.omg.CORBA.CharSeqHolder conformance: holder value field.
 * @run main CharSeqHolderTest
 */

public class CharSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.CharSeqHolder h0 = new org.omg.CORBA.CharSeqHolder();
        check(h0.value == null, "no-arg default value");
        char[] v = null;
        org.omg.CORBA.CharSeqHolder h1 = new org.omg.CORBA.CharSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
