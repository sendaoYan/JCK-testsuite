/*
 * @test
 * @summary org.omg.CORBA.portable.CustomValue conformance: interface shape.
 * @run main CustomValueTest
 */

public class CustomValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.CustomValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");
        check(org.omg.CORBA.CustomMarshal.class.isAssignableFrom(c), "extends org.omg.CORBA.CustomMarshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
