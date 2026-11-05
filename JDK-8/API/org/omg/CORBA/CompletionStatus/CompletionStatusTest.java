/*
 * @test
 * @summary org.omg.CORBA.CompletionStatus conformance: enumeration values.
 * @run main CompletionStatusTest
 */

public class CompletionStatusTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.CompletionStatus");
        check(!c.isInterface(), "is a class");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements IDLEntity");

        // ---- three constants with precise values ----
        check(org.omg.CORBA.CompletionStatus.COMPLETED_YES.value() == 0, "COMPLETED_YES == 0");
        check(org.omg.CORBA.CompletionStatus.COMPLETED_NO.value() == 1, "COMPLETED_NO == 1");
        check(org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE.value() == 2, "COMPLETED_MAYBE == 2");
        check(org.omg.CORBA.CompletionStatus.COMPLETED_YES != org.omg.CORBA.CompletionStatus.COMPLETED_NO,
              "constants are distinct instances");
        check(org.omg.CORBA.CompletionStatus.COMPLETED_NO != org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE,
              "NO and MAYBE distinct");

        // ---- from_int normalization for every constant ----
        org.omg.CORBA.CompletionStatus[] all = {
            org.omg.CORBA.CompletionStatus.COMPLETED_YES,
            org.omg.CORBA.CompletionStatus.COMPLETED_NO,
            org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE };
        for (org.omg.CORBA.CompletionStatus cs : all) {
            check(org.omg.CORBA.CompletionStatus.from_int(cs.value()) == cs,
                  "from_int(" + cs.value() + ") normalizes to same constant");
        }
        check(org.omg.CORBA.CompletionStatus.from_int(0) == org.omg.CORBA.CompletionStatus.COMPLETED_YES,
              "from_int(0) == COMPLETED_YES");
        check(org.omg.CORBA.CompletionStatus.from_int(1) == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
              "from_int(1) == COMPLETED_NO");
        check(org.omg.CORBA.CompletionStatus.from_int(2) == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE,
              "from_int(2) == COMPLETED_MAYBE");

        // ---- out-of-range -> BAD_PARAM ----
        try {
            org.omg.CORBA.CompletionStatus.from_int(9);
            throw new AssertionError("from_int(9) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(9) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.CompletionStatus.from_int(3);
            throw new AssertionError("from_int(3) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(3) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.CompletionStatus.from_int(-1);
            throw new AssertionError("from_int(-1) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(-1) -> BAD_PARAM");
        }

        // ---- toString: default Object form on jdk8u372 (no name embedded) ----
        check(org.omg.CORBA.CompletionStatus.COMPLETED_YES.toString().startsWith("org.omg.CORBA.CompletionStatus@"),
              "toString is default Object form");

        // ---- usable as SystemException.completed field ----
        org.omg.CORBA.MARSHAL m = new org.omg.CORBA.MARSHAL("x", 5, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        check(m.completed == org.omg.CORBA.CompletionStatus.COMPLETED_YES, "usable in SystemException.completed");


        // legacy smoke assertions (restored verbatim: 只增不删)
        check(org.omg.CORBA.CompletionStatus.COMPLETED_YES != null, "COMPLETED_YES constant non-null");
        check(org.omg.CORBA.CompletionStatus.COMPLETED_YES.value() == org.omg.CORBA.CompletionStatus._COMPLETED_YES, "value() matches int constant");
        check(org.omg.CORBA.CompletionStatus.from_int(org.omg.CORBA.CompletionStatus._COMPLETED_YES) == org.omg.CORBA.CompletionStatus.COMPLETED_YES, "from_int round-trips");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
