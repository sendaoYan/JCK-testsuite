/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptor_3_0 conformance: interface shape.
 * @run main IORInterceptor_3_0Test
 */

public class IORInterceptor_3_0Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.IORInterceptor_3_0");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Operations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.IORInterceptor_3_0Operations");
        check(org.omg.PortableInterceptor.IORInterceptor.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.IORInterceptor");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
