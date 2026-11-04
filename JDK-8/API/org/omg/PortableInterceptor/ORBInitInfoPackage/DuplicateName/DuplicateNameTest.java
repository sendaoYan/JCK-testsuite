/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName conformance: UserException hierarchy and constructors.
 * @run main DuplicateNameTest
 */

public class DuplicateNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class), "implements IDLEntity");
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e = new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName();
        check(e.name == null, "name default null");
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName em = new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName("msg", null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
