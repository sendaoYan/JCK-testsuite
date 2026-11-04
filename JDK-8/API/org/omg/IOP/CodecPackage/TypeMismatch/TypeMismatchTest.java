/*
 * @test
 * @summary org.omg.IOP.CodecPackage.TypeMismatch conformance: UserException hierarchy and constructors.
 * @run main TypeMismatchTest
 */

public class TypeMismatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.IOP.CodecPackage.TypeMismatch.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.IOP.CodecPackage.TypeMismatch.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.CodecPackage.TypeMismatch.class), "implements IDLEntity");
        org.omg.IOP.CodecPackage.TypeMismatch e = new org.omg.IOP.CodecPackage.TypeMismatch();
        org.omg.IOP.CodecPackage.TypeMismatch em = new org.omg.IOP.CodecPackage.TypeMismatch("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
