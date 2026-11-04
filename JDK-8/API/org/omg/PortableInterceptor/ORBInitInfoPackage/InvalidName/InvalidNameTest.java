/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName conformance: UserException hierarchy and constructors.
 * @run main InvalidNameTest
 */

public class InvalidNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class), "implements IDLEntity");
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName e = new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName();
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName em = new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
