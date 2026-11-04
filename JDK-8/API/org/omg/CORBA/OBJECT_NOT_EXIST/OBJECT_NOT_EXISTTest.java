/*
 * @test
 * @summary org.omg.CORBA.OBJECT_NOT_EXIST conformance: SystemException hierarchy and constructors.
 * @run main OBJECT_NOT_EXISTTest
 */

public class OBJECT_NOT_EXISTTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.SystemException.class.isAssignableFrom(org.omg.CORBA.OBJECT_NOT_EXIST.class), "extends SystemException");
        check(java.lang.RuntimeException.class.isAssignableFrom(org.omg.CORBA.OBJECT_NOT_EXIST.class), "is unchecked RuntimeException");
        org.omg.CORBA.OBJECT_NOT_EXIST e0 = new org.omg.CORBA.OBJECT_NOT_EXIST();
        check(e0.minor == 0, "minor default 0");
        check(e0.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO, "completed default COMPLETED_NO");
        org.omg.CORBA.OBJECT_NOT_EXIST e1 = new org.omg.CORBA.OBJECT_NOT_EXIST("msg");
        check("msg".equals(e1.getMessage()), "message ctor");
        org.omg.CORBA.OBJECT_NOT_EXIST e2 = new org.omg.CORBA.OBJECT_NOT_EXIST(7, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        check(e2.minor == 7, "minor ctor");
        check(e2.completed == org.omg.CORBA.CompletionStatus.COMPLETED_YES, "completed ctor");
        org.omg.CORBA.OBJECT_NOT_EXIST e3 = new org.omg.CORBA.OBJECT_NOT_EXIST("m2", 8, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check("m2".equals(e3.getMessage()), "full ctor message");
        check(e3.minor == 8, "full ctor minor");
        check(e3.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE, "full ctor completed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
