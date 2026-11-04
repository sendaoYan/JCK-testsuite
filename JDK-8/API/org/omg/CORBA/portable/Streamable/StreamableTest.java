/*
 * @test
 * @summary org.omg.CORBA.portable.Streamable conformance: interface shape.
 * @run main StreamableTest
 */

public class StreamableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.Streamable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
