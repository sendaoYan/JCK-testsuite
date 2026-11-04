/*
 * @test
 * @summary org.omg.IOP.CodecFactoryPackage.UnknownEncoding conformance: UserException hierarchy and constructors.
 * @run main UnknownEncodingTest
 */

public class UnknownEncodingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class), "implements IDLEntity");
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding e = new org.omg.IOP.CodecFactoryPackage.UnknownEncoding();
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding em = new org.omg.IOP.CodecFactoryPackage.UnknownEncoding("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
