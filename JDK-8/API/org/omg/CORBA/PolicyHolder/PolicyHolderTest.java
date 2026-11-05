/*
 * @test
 * @summary org.omg.CORBA.PolicyHolder conformance: holder value field.
 * @run main PolicyHolderTest
 */

public class PolicyHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.PolicyHolder h0 = new org.omg.CORBA.PolicyHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Policy v = null;
        org.omg.CORBA.PolicyHolder h1 = new org.omg.CORBA.PolicyHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.PolicyHolder().value == null, "default value is null");
        check(new org.omg.CORBA.PolicyHolder(null).value == null, "ctor stores null");
        check(new org.omg.CORBA.PolicyHolder()._type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "PolicyHolder._type().kind() == tk_objref");
        org.omg.CORBA.portable.OutputStream osPh = orb.create_output_stream();
        new org.omg.CORBA.PolicyHolder(null)._write(osPh);
        org.omg.CORBA.PolicyHolder whPh = new org.omg.CORBA.PolicyHolder();
        whPh._read(osPh.create_input_stream());
        check(whPh.value == null, "nil objref wire round-trip reads back null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
