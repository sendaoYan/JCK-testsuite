/*
 * @test
 * @summary org.omg.CORBA.DoubleHolder conformance: holder value field.
 * @run main DoubleHolderTest
 */

public class DoubleHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.DoubleHolder h0 = new org.omg.CORBA.DoubleHolder();
        check(h0.value == 0.0, "no-arg default value");
        double v = 7.0;
        org.omg.CORBA.DoubleHolder h1 = new org.omg.CORBA.DoubleHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
