/*
 * @test
 * @summary org.omg.IOP.IOR conformance: value type fields and constructors.
 * @run main IORTest
 */

public class IORTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.IOR s0 = new org.omg.IOP.IOR();
        check(s0.type_id == null, "no-arg type_id default");
        check(s0.profiles == null, "no-arg profiles default");
        org.omg.IOP.IOR s1 = new org.omg.IOP.IOR("s", null);
        check(s1.type_id == "s", "ctor type_id stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
