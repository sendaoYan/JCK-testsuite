/*
 * @test
 * @summary org.omg.PortableServer.POA conformance: interface shape.
 * @run main POATest
 */

public class POATest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.POA");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.POAOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.POAOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
