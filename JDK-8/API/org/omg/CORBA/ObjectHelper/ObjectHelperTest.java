/*
 * @test
 * @summary org.omg.CORBA.ObjectHelper conformance: helper static id/type/narrow.
 * @run main ObjectHelperTest
 */

public class ObjectHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ObjectHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ObjectHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ObjectHelper.id().equals(""),
              "ObjectHelper.id() is the empty string (Object has no repository id, jdk8u372 quirk)");
        check(org.omg.CORBA.ObjectHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "ObjectHelper.type().kind() == tk_objref");
        check(new org.omg.CORBA.ObjectHolder()._type().equal(org.omg.CORBA.ObjectHelper.type()),
              "ObjectHolder._type().equal(ObjectHelper.type())");
        org.omg.CORBA.Any anyOh = orb.create_any();
        org.omg.CORBA.ObjectHelper.insert(anyOh, null);
        check(org.omg.CORBA.ObjectHelper.extract(anyOh) == null, "insert(null)/extract round-trips null");
        org.omg.CORBA.portable.OutputStream osOh = orb.create_output_stream();
        org.omg.CORBA.ObjectHelper.write(osOh, null);
        check(org.omg.CORBA.ObjectHelper.read(osOh.create_input_stream()) == null,
              "write(null)/read CDR round-trips nil objref");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
