/*
 * @test
 * @summary org.omg.CORBA.portable.StreamableValue conformance: interface shape.
 * @run main StreamableValueTest
 */

public class StreamableValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.StreamableValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.Streamable");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
