/*
 * @test
 * @summary org.omg.CORBA.TCKind conformance: enumeration values.
 * @run main TCKindTest
 */

public class TCKindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.TCKind.tk_null != null, "tk_null constant non-null");
        check(org.omg.CORBA.TCKind.tk_null.value() == org.omg.CORBA.TCKind._tk_null, "value() matches int constant");
        check(org.omg.CORBA.TCKind.from_int(org.omg.CORBA.TCKind._tk_null) == org.omg.CORBA.TCKind.tk_null, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
