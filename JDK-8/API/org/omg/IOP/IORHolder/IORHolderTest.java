/*
 * @test
 * @summary org.omg.IOP.IORHolder conformance: holder value field.
 * @run main IORHolderTest
 */

public class IORHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.IORHolder h0 = new org.omg.IOP.IORHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.IOR v = null;
        org.omg.IOP.IORHolder h1 = new org.omg.IOP.IORHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
