/*
 * @test
 * @summary org.omg.CORBA.BAD_CONTEXT conformance: SystemException hierarchy and constructors.
 * @run main BAD_CONTEXTTest
 */

public class BAD_CONTEXTTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.SystemException.class.isAssignableFrom(org.omg.CORBA.BAD_CONTEXT.class), "extends SystemException");
        check(java.lang.RuntimeException.class.isAssignableFrom(org.omg.CORBA.BAD_CONTEXT.class), "is unchecked RuntimeException");
        org.omg.CORBA.BAD_CONTEXT e0 = new org.omg.CORBA.BAD_CONTEXT();
        check(e0.minor == 0, "minor default 0");
        check(e0.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO, "completed default COMPLETED_NO");
        org.omg.CORBA.BAD_CONTEXT e1 = new org.omg.CORBA.BAD_CONTEXT("msg");
        check("msg".equals(e1.getMessage()), "message ctor");
        org.omg.CORBA.BAD_CONTEXT e2 = new org.omg.CORBA.BAD_CONTEXT(7, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        check(e2.minor == 7, "minor ctor");
        check(e2.completed == org.omg.CORBA.CompletionStatus.COMPLETED_YES, "completed ctor");
        org.omg.CORBA.BAD_CONTEXT e3 = new org.omg.CORBA.BAD_CONTEXT("m2", 8, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check("m2".equals(e3.getMessage()), "full ctor message");
        check(e3.minor == 8, "full ctor minor");
        check(e3.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE, "full ctor completed");

        // ---- deepened: toString format and family membership (jdk8u372 measured) ----
        org.omg.CORBA.BAD_CONTEXT t0 = new org.omg.CORBA.BAD_CONTEXT();
        check(t0.toString().startsWith("org.omg.CORBA.BAD_CONTEXT: "), "BAD_CONTEXT default toString class prefix");
        check(t0.toString().contains("vmcid: 0x0"), "BAD_CONTEXT default toString vmcid 0x0");
        check(t0.toString().contains("minor code: 0"), "BAD_CONTEXT default toString minor code: 0");
        check(t0.toString().contains("completed: No"), "BAD_CONTEXT default toString completed: No");
        check(t0 instanceof org.omg.CORBA.SystemException, "BAD_CONTEXT instanceof SystemException");
        org.omg.CORBA.BAD_CONTEXT tF = new org.omg.CORBA.BAD_CONTEXT("m", 77, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check(tF.toString().contains("minor code: 77"), "BAD_CONTEXT full ctor toString minor 77");
        check(tF.toString().contains("completed: Maybe"), "BAD_CONTEXT full ctor toString completed: Maybe");
        check(tF.minor == 77 && tF.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE,
              "BAD_CONTEXT full ctor field readback");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
