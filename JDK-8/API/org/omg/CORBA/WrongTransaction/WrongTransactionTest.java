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

        // ---- deepened: both ctors carry the repository id as message (jdk8u372 measured) ----
        check(e.getMessage() != null, "() message non-null: repository id");
        check("IDL:omg.org/CORBA/WrongTransaction:1.0".equals(e.getMessage()),
              "() message is the repository id");
        check("IDL:omg.org/CORBA/WrongTransaction:1.0  msg".equals(em.getMessage()),
              "(String) ctor message is repository id + two spaces + reason (jdk8u372)");
        check(em.getMessage().startsWith("IDL:omg.org/CORBA/WrongTransaction:1.0"),
              "(String) ctor message starts with the repository id");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.WrongTransaction.class.getModifiers()),
              "WrongTransaction is final");
        check(org.omg.CORBA.WrongTransaction.class.getSuperclass() == org.omg.CORBA.UserException.class,
              "direct UserException subclass");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
