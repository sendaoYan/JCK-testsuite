/*
 * @test
 * @summary org.omg.IOP.ServiceContext conformance: value type fields and constructors.
 * @run main ServiceContextTest
 */

public class ServiceContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.ServiceContext s0 = new org.omg.IOP.ServiceContext();
        check(s0.context_id == 0, "no-arg context_id default");
        check(s0.context_data == null, "no-arg context_data default");
        org.omg.IOP.ServiceContext s1 = new org.omg.IOP.ServiceContext(7, null);
        check(s1.context_id == 7, "ctor context_id stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
