/*
 * @test
 * @summary org.omg.CORBA.WrongTransaction conformance: UserException hierarchy and constructors.
 * @run main WrongTransactionTest
 */

public class WrongTransactionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.WrongTransaction.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.WrongTransaction.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.WrongTransaction.class), "implements IDLEntity");
        org.omg.CORBA.WrongTransaction e = new org.omg.CORBA.WrongTransaction();
        org.omg.CORBA.WrongTransaction em = new org.omg.CORBA.WrongTransaction("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
