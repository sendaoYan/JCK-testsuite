/*
 * @test
 * @summary org.omg.CORBA.PolicyListHelper conformance: helper static id/type/narrow.
 * @run main PolicyListHelperTest
 */

public class PolicyListHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyListHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyListHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.PolicyListHelper.id().equals("IDL:omg.org/CORBA/PolicyList:1.0"),
              "PolicyListHelper.id() exact repository id");
        check(org.omg.CORBA.PolicyListHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "PolicyListHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osPl = orb.create_output_stream();
        org.omg.CORBA.PolicyListHelper.write(osPl, new org.omg.CORBA.Policy[]{null});
        org.omg.CORBA.Policy[] rtPl = org.omg.CORBA.PolicyListHelper.read(osPl.create_input_stream());
        check(rtPl.length == 1 && rtPl[0] == null, "write/read CDR round-trip with nil element");
        org.omg.CORBA.Any anyPl = orb.create_any();
        org.omg.CORBA.PolicyListHelper.insert(anyPl, new org.omg.CORBA.Policy[]{null});
        org.omg.CORBA.Policy[] xPl = org.omg.CORBA.PolicyListHelper.extract(anyPl);
        check(xPl.length == 1 && xPl[0] == null, "insert/extract Any round-trip with nil element");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
