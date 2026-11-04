/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceFactory conformance: interface shape.
 * @run main ObjectReferenceFactoryTest
 */

public class ObjectReferenceFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ObjectReferenceFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
