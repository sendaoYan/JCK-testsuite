/*
 * @test
 * @summary org.omg.CORBA.DataOutputStream conformance: interface shape.
 * @run main DataOutputStreamTest
 */

public class DataOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DataOutputStream");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
