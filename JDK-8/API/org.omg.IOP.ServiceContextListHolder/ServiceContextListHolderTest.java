/*
 * @test
 * @summary org.omg.IOP.ServiceContextListHolder conformance: holder value field.
 * @run main ServiceContextListHolderTest
 */

public class ServiceContextListHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.ServiceContextListHolder h0 = new org.omg.IOP.ServiceContextListHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.ServiceContext[] v = null;
        org.omg.IOP.ServiceContextListHolder h1 = new org.omg.IOP.ServiceContextListHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
