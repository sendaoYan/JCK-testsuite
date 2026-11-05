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
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.ParameterModeHolder().value == null, "default value is null");
        check(new org.omg.CORBA.ParameterModeHolder(org.omg.CORBA.ParameterMode.PARAM_OUT).value
              == org.omg.CORBA.ParameterMode.PARAM_OUT, "ctor stores enum reference");
        check(new org.omg.CORBA.ParameterModeHolder()._type().kind() == org.omg.CORBA.TCKind.tk_enum,
              "ParameterModeHolder._type().kind() == tk_enum");
        org.omg.CORBA.portable.OutputStream osPm = orb.create_output_stream();
        new org.omg.CORBA.ParameterModeHolder(org.omg.CORBA.ParameterMode.PARAM_OUT)._write(osPm);
        org.omg.CORBA.ParameterModeHolder whPm = new org.omg.CORBA.ParameterModeHolder();
        whPm._read(osPm.create_input_stream());
        check(whPm.value.value() == org.omg.CORBA.ParameterMode._PARAM_OUT,
              "ParameterMode CDR wire round-trip value");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
