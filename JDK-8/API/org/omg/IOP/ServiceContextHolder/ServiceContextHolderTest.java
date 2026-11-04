/*
 * @test
 * @summary org.omg.IOP.ServiceContextHolder conformance: holder value field.
 * @run main ServiceContextHolderTest
 */

public class ServiceContextHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.ServiceContextHolder h0 = new org.omg.IOP.ServiceContextHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.ServiceContext v = null;
        org.omg.IOP.ServiceContextHolder h1 = new org.omg.IOP.ServiceContextHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
