/*
 * @test
 * @summary org.omg.CORBA.UnknownUserException conformance: UserException hierarchy and constructors.
 * @run main UnknownUserExceptionTest
 */

public class UnknownUserExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.UnknownUserException.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.UnknownUserException.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.UnknownUserException.class), "implements IDLEntity");
        org.omg.CORBA.UnknownUserException e = new org.omg.CORBA.UnknownUserException();
        check(e.except == null, "except default null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
