/*
 * @test
 * @summary org.omg.CORBA.AnyHolder conformance: holder value field.
 * @run main AnyHolderTest
 */

public class AnyHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.AnyHolder h0 = new org.omg.CORBA.AnyHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Any v = null;
        org.omg.CORBA.AnyHolder h1 = new org.omg.CORBA.AnyHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
