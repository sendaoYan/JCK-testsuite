/*
 * @test
 * @summary org.omg.IOP.CodecPackage.InvalidTypeForEncoding conformance: UserException hierarchy and constructors.
 * @run main InvalidTypeForEncodingTest
 */

public class InvalidTypeForEncodingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.IOP.CodecPackage.InvalidTypeForEncoding.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.IOP.CodecPackage.InvalidTypeForEncoding.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.CodecPackage.InvalidTypeForEncoding.class), "implements IDLEntity");
        org.omg.IOP.CodecPackage.InvalidTypeForEncoding e = new org.omg.IOP.CodecPackage.InvalidTypeForEncoding();
        org.omg.IOP.CodecPackage.InvalidTypeForEncoding em = new org.omg.IOP.CodecPackage.InvalidTypeForEncoding("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
