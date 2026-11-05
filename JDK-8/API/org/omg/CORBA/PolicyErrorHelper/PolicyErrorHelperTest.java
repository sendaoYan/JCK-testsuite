/*
 * @test
 * @summary org.omg.CORBA.PolicyErrorHelper conformance: helper static id/type/narrow.
 * @run main PolicyErrorHelperTest
 */

public class PolicyErrorHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyErrorHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyErrorHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.PolicyErrorHelper.id().equals("IDL:omg.org/CORBA/PolicyError:1.0"),
              "PolicyErrorHelper.id() exact repository id");
        check(org.omg.CORBA.PolicyErrorHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "PolicyErrorHelper.type().kind() == tk_except");
        org.omg.CORBA.portable.OutputStream osPe = orb.create_output_stream();
        org.omg.CORBA.PolicyErrorHelper.write(osPe, new org.omg.CORBA.PolicyError("bad", (short) 2));
        org.omg.CORBA.PolicyError rtPe = org.omg.CORBA.PolicyErrorHelper.read(osPe.create_input_stream());
        check(rtPe.reason == 2, "write/read CDR round-trip keeps reason");
        check(rtPe.getMessage() == null, "wire read-back drops the ctor message (only reason is marshalled)");
        org.omg.CORBA.Any anyPe = orb.create_any();
        org.omg.CORBA.PolicyErrorHelper.insert(anyPe, new org.omg.CORBA.PolicyError((short) 3));
        check(org.omg.CORBA.PolicyErrorHelper.extract(anyPe).reason == 3, "insert/extract Any round-trip keeps reason");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
