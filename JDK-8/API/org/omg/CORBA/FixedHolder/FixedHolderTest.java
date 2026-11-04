/*
 * @test
 * @summary org.omg.CORBA.FixedHolder conformance: holder value field.
 * @run main FixedHolderTest
 */

public class FixedHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.FixedHolder h0 = new org.omg.CORBA.FixedHolder();
        check(h0.value == null, "no-arg default value");
        java.math.BigDecimal v = new java.math.BigDecimal("1.5");
        org.omg.CORBA.FixedHolder h1 = new org.omg.CORBA.FixedHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
