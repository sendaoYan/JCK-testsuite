/*
 * @test
 * @summary org.omg.CORBA.SetOverrideType conformance: enumeration values.
 * @run main SetOverrideTypeTest
 */

public class SetOverrideTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.SetOverrideType.SET_OVERRIDE != null, "SET_OVERRIDE constant non-null");
        check(org.omg.CORBA.SetOverrideType.SET_OVERRIDE.value() == org.omg.CORBA.SetOverrideType._SET_OVERRIDE, "value() matches int constant");
        check(org.omg.CORBA.SetOverrideType.from_int(org.omg.CORBA.SetOverrideType._SET_OVERRIDE) == org.omg.CORBA.SetOverrideType.SET_OVERRIDE, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
