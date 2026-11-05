/*
 * @test
 * @summary org.omg.CORBA.PolicyHelper conformance: helper static id/type/narrow.
 * @run main PolicyHelperTest
 */

public class PolicyHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyHelper.type() != null, "type() non-null");
        check(org.omg.CORBA.PolicyHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.PolicyHelper.id().equals("IDL:omg.org/CORBA/Policy:1.0"),
              "PolicyHelper.id() exact repository id");
        check(org.omg.CORBA.PolicyHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "PolicyHelper.type().kind() == tk_objref");
        org.omg.CORBA.Any anyPh = orb.create_any();
        org.omg.CORBA.PolicyHelper.insert(anyPh, null);
        check(org.omg.CORBA.PolicyHelper.extract(anyPh) == null, "insert(null)/extract round-trips null");
        check(org.omg.CORBA.PolicyHelper.narrow(null) == null, "narrow(null) returns null");
        try {
            org.omg.CORBA.Any anyPh2 = orb.create_any(); anyPh2.insert_long(1); org.omg.CORBA.PolicyHelper.extract(anyPh2);
            throw new AssertionError("PolicyHelper.extract over a tk_long any should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "PolicyHelper.extract over a tk_long any -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
