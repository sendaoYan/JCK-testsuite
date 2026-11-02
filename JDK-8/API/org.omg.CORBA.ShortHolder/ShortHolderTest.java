/*
 * @test
 * @summary org.omg.CORBA.ShortHolder conformance: holder value field.
 * @run main ShortHolderTest
 */

public class ShortHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ShortHolder h0 = new org.omg.CORBA.ShortHolder();
        check(h0.value == (short)0, "no-arg default value");
        short v = (short)7;
        org.omg.CORBA.ShortHolder h1 = new org.omg.CORBA.ShortHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
