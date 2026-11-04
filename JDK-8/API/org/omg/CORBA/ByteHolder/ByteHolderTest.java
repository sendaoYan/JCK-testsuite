/*
 * @test
 * @summary org.omg.CORBA.ByteHolder conformance: holder value field.
 * @run main ByteHolderTest
 */

public class ByteHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ByteHolder h0 = new org.omg.CORBA.ByteHolder();
        check(h0.value == (byte)0, "no-arg default value");
        byte v = (byte)7;
        org.omg.CORBA.ByteHolder h1 = new org.omg.CORBA.ByteHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
