/*
 * @test
 * @summary org.omg.CORBA.BAD_INV_ORDER conformance: SystemException hierarchy and constructors.
 * @run main BAD_INV_ORDERTest
 */

public class BAD_INV_ORDERTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.SystemException.class.isAssignableFrom(org.omg.CORBA.BAD_INV_ORDER.class), "extends SystemException");
        check(java.lang.RuntimeException.class.isAssignableFrom(org.omg.CORBA.BAD_INV_ORDER.class), "is unchecked RuntimeException");
        org.omg.CORBA.BAD_INV_ORDER e0 = new org.omg.CORBA.BAD_INV_ORDER();
        check(e0.minor == 0, "minor default 0");
        check(e0.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO, "completed default COMPLETED_NO");
        org.omg.CORBA.BAD_INV_ORDER e1 = new org.omg.CORBA.BAD_INV_ORDER("msg");
        check("msg".equals(e1.getMessage()), "message ctor");
        org.omg.CORBA.BAD_INV_ORDER e2 = new org.omg.CORBA.BAD_INV_ORDER(7, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        check(e2.minor == 7, "minor ctor");
        check(e2.completed == org.omg.CORBA.CompletionStatus.COMPLETED_YES, "completed ctor");
        org.omg.CORBA.BAD_INV_ORDER e3 = new org.omg.CORBA.BAD_INV_ORDER("m2", 8, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check("m2".equals(e3.getMessage()), "full ctor message");
        check(e3.minor == 8, "full ctor minor");
        check(e3.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE, "full ctor completed");

        // ---- deepened: toString format and family membership (jdk8u372 measured) ----
        org.omg.CORBA.BAD_INV_ORDER t0 = new org.omg.CORBA.BAD_INV_ORDER();
        check(t0.toString().startsWith("org.omg.CORBA.BAD_INV_ORDER: "), "BAD_INV_ORDER default toString class prefix");
        check(t0.toString().contains("vmcid: 0x0"), "BAD_INV_ORDER default toString vmcid 0x0");
        check(t0.toString().contains("minor code: 0"), "BAD_INV_ORDER default toString minor code: 0");
        check(t0.toString().contains("completed: No"), "BAD_INV_ORDER default toString completed: No");
        check(t0 instanceof org.omg.CORBA.SystemException, "BAD_INV_ORDER instanceof SystemException");
        org.omg.CORBA.BAD_INV_ORDER tF = new org.omg.CORBA.BAD_INV_ORDER("m", 77, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check(tF.toString().contains("minor code: 77"), "BAD_INV_ORDER full ctor toString minor 77");
        check(tF.toString().contains("completed: Maybe"), "BAD_INV_ORDER full ctor toString completed: Maybe");
        check(tF.minor == 77 && tF.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE,
              "BAD_INV_ORDER full ctor field readback");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
