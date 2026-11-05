/*
 * @test
 * @summary org.omg.CORBA.COMM_FAILURE conformance: SystemException hierarchy and constructors.
 * @run main COMM_FAILURETest
 */

public class COMM_FAILURETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.SystemException.class.isAssignableFrom(org.omg.CORBA.COMM_FAILURE.class), "extends SystemException");
        check(java.lang.RuntimeException.class.isAssignableFrom(org.omg.CORBA.COMM_FAILURE.class), "is unchecked RuntimeException");
        org.omg.CORBA.COMM_FAILURE e0 = new org.omg.CORBA.COMM_FAILURE();
        check(e0.minor == 0, "minor default 0");
        check(e0.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO, "completed default COMPLETED_NO");
        org.omg.CORBA.COMM_FAILURE e1 = new org.omg.CORBA.COMM_FAILURE("msg");
        check("msg".equals(e1.getMessage()), "message ctor");
        org.omg.CORBA.COMM_FAILURE e2 = new org.omg.CORBA.COMM_FAILURE(7, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        check(e2.minor == 7, "minor ctor");
        check(e2.completed == org.omg.CORBA.CompletionStatus.COMPLETED_YES, "completed ctor");
        org.omg.CORBA.COMM_FAILURE e3 = new org.omg.CORBA.COMM_FAILURE("m2", 8, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check("m2".equals(e3.getMessage()), "full ctor message");
        check(e3.minor == 8, "full ctor minor");
        check(e3.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE, "full ctor completed");

        // ---- deepened: toString format and family membership (jdk8u372 measured) ----
        org.omg.CORBA.COMM_FAILURE t0 = new org.omg.CORBA.COMM_FAILURE();
        check(t0.toString().startsWith("org.omg.CORBA.COMM_FAILURE: "), "COMM_FAILURE default toString class prefix");
        check(t0.toString().contains("vmcid: 0x0"), "COMM_FAILURE default toString vmcid 0x0");
        check(t0.toString().contains("minor code: 0"), "COMM_FAILURE default toString minor code: 0");
        check(t0.toString().contains("completed: No"), "COMM_FAILURE default toString completed: No");
        check(t0 instanceof org.omg.CORBA.SystemException, "COMM_FAILURE instanceof SystemException");
        org.omg.CORBA.COMM_FAILURE tF = new org.omg.CORBA.COMM_FAILURE("m", 77, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check(tF.toString().contains("minor code: 77"), "COMM_FAILURE full ctor toString minor 77");
        check(tF.toString().contains("completed: Maybe"), "COMM_FAILURE full ctor toString completed: Maybe");
        check(tF.minor == 77 && tF.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE,
              "COMM_FAILURE full ctor field readback");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
