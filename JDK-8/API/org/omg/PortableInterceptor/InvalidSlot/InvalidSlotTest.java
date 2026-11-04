/*
 * @test
 * @summary org.omg.PortableInterceptor.InvalidSlot conformance: UserException hierarchy and constructors.
 * @run main InvalidSlotTest
 */

public class InvalidSlotTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.InvalidSlot.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.InvalidSlot.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.InvalidSlot.class), "implements IDLEntity");
        org.omg.PortableInterceptor.InvalidSlot e = new org.omg.PortableInterceptor.InvalidSlot();
        org.omg.PortableInterceptor.InvalidSlot em = new org.omg.PortableInterceptor.InvalidSlot("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
