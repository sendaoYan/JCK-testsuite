/*
 * @test
 * @summary org.omg.CORBA.ParameterMode conformance: enumeration values.
 * @run main ParameterModeTest
 */

public class ParameterModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ParameterMode");
        check(!c.isInterface(), "is a class");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements IDLEntity");

        // ---- full table PARAM_IN/OUT/INOUT {0,1,2} ----
        check(org.omg.CORBA.ParameterMode.PARAM_IN.value() == 0, "PARAM_IN == 0");
        check(org.omg.CORBA.ParameterMode.PARAM_OUT.value() == 1, "PARAM_OUT == 1");
        check(org.omg.CORBA.ParameterMode.PARAM_INOUT.value() == 2, "PARAM_INOUT == 2");
        org.omg.CORBA.ParameterMode[] all = {
            org.omg.CORBA.ParameterMode.PARAM_IN,
            org.omg.CORBA.ParameterMode.PARAM_OUT,
            org.omg.CORBA.ParameterMode.PARAM_INOUT };
        for (org.omg.CORBA.ParameterMode p : all) {
            check(org.omg.CORBA.ParameterMode.from_int(p.value()) == p,
                  "from_int(" + p.value() + ") normalizes to same constant");
        }
        check(org.omg.CORBA.ParameterMode.from_int(0) == org.omg.CORBA.ParameterMode.PARAM_IN, "from_int(0)==PARAM_IN");
        check(org.omg.CORBA.ParameterMode.from_int(1) == org.omg.CORBA.ParameterMode.PARAM_OUT, "from_int(1)==PARAM_OUT");
        check(org.omg.CORBA.ParameterMode.from_int(2) == org.omg.CORBA.ParameterMode.PARAM_INOUT, "from_int(2)==PARAM_INOUT");

        // ---- out-of-range -> BAD_PARAM ----
        try {
            org.omg.CORBA.ParameterMode.from_int(5);
            throw new AssertionError("from_int(5) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(5) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.ParameterMode.from_int(3);
            throw new AssertionError("from_int(3) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(3) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.ParameterMode.from_int(-1);
            throw new AssertionError("from_int(-1) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(-1) -> BAD_PARAM");
        }

        // jdk8u372: toString is default Object form
        check(org.omg.CORBA.ParameterMode.PARAM_IN.toString().startsWith("org.omg.CORBA.ParameterMode@"),
              "toString is default Object form");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
