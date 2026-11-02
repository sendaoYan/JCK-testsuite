/*
 * @test
 * @summary org.omg.IOP.CodecPackage.FormatMismatch conformance: UserException hierarchy and constructors.
 * @run main FormatMismatchTest
 */

public class FormatMismatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.IOP.CodecPackage.FormatMismatch.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.IOP.CodecPackage.FormatMismatch.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.CodecPackage.FormatMismatch.class), "implements IDLEntity");
        org.omg.IOP.CodecPackage.FormatMismatch e = new org.omg.IOP.CodecPackage.FormatMismatch();
        org.omg.IOP.CodecPackage.FormatMismatch em = new org.omg.IOP.CodecPackage.FormatMismatch("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
