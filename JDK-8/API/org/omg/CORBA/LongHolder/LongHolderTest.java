/*
 * @test
 * @summary org.omg.CORBA.LongHolder conformance: holder value field.
 * @run main LongHolderTest
 */

public class LongHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LongHolder h0 = new org.omg.CORBA.LongHolder();
        check(h0.value == 0L, "no-arg default value");
        long v = 7L;
        org.omg.CORBA.LongHolder h1 = new org.omg.CORBA.LongHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
