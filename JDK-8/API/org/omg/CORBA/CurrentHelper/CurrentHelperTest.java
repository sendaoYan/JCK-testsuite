/*
 * @test
 * @summary org.omg.CORBA.CurrentHelper conformance: helper static id/type/narrow.
 * @run main CurrentHelperTest
 */

public class CurrentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.CurrentHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.CurrentHelper.type() != null, "type() non-null");
        check(org.omg.CORBA.CurrentHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.CurrentHelper.id().equals("IDL:omg.org/CORBA/Current:1.0"),
              "CurrentHelper.id() exact repository id");
        check(org.omg.CORBA.CurrentHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "CurrentHelper.type().kind() == tk_objref");
        check(org.omg.CORBA.CurrentHelper.narrow(null) == null, "narrow(null) returns null");
        try {
            org.omg.CORBA.Any anyCh = orb.create_any(); org.omg.CORBA.CurrentHelper.insert(anyCh, null);
            throw new AssertionError("CurrentHelper.insert(null) (abstract valuetype Current unmarshalable offline, jdk8u372) should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "CurrentHelper.insert(null) (abstract valuetype Current unmarshalable offline, jdk8u372) -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
