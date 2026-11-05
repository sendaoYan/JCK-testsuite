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

        // ---- deepened: (Any) ctor keeps reference, default state ----
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.Any cause = orb.create_any();
        cause.insert_string("boom");
        org.omg.CORBA.UnknownUserException withAny = new org.omg.CORBA.UnknownUserException(cause);
        check(withAny.except == cause, "(Any) ctor keeps the same Any reference");
        check("boom".equals(withAny.except.extract_string()), "carried Any still readable");
        check(e.getMessage() == null, "() default message null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.UnknownUserException.class.getModifiers()),
              "UnknownUserException is final");
        check(org.omg.CORBA.UnknownUserException.class.getConstructors().length == 2,
              "exposes exactly () and (Any) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
