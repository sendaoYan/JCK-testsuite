/*
 * @test
 * @summary org.omg.CORBA.ParameterModeHolder conformance: holder value field.
 * @run main ParameterModeHolderTest
 */

public class ParameterModeHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ParameterModeHolder h0 = new org.omg.CORBA.ParameterModeHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.ParameterMode v = null;
        org.omg.CORBA.ParameterModeHolder h1 = new org.omg.CORBA.ParameterModeHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
