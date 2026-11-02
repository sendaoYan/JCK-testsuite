/*
 * @test
 * @summary org.omg.CORBA.DynAnyPackage.InvalidSeq conformance: UserException hierarchy and constructors.
 * @run main InvalidSeqTest
 */

public class InvalidSeqTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidSeq.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidSeq.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidSeq.class), "implements IDLEntity");
        org.omg.CORBA.DynAnyPackage.InvalidSeq e = new org.omg.CORBA.DynAnyPackage.InvalidSeq();
        org.omg.CORBA.DynAnyPackage.InvalidSeq em = new org.omg.CORBA.DynAnyPackage.InvalidSeq("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
