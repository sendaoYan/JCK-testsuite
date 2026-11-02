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
        check(org.omg.CORBA.ParameterMode.PARAM_IN != null, "PARAM_IN constant non-null");
        check(org.omg.CORBA.ParameterMode.PARAM_IN.value() == org.omg.CORBA.ParameterMode._PARAM_IN, "value() matches int constant");
        check(org.omg.CORBA.ParameterMode.from_int(org.omg.CORBA.ParameterMode._PARAM_IN) == org.omg.CORBA.ParameterMode.PARAM_IN, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
