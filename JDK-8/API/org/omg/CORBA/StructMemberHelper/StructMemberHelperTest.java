/*
 * @test
 * @summary org.omg.CORBA.StructMemberHelper conformance: helper static id/type/narrow.
 * @run main StructMemberHelperTest
 */

public class StructMemberHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.StructMemberHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.StructMemberHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.StructMemberHelper.id().equals("IDL:omg.org/CORBA/StructMember:1.0"),
              "StructMemberHelper.id() exact repository id");
        check(org.omg.CORBA.StructMemberHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "StructMemberHelper.type().kind() == tk_struct");
        org.omg.CORBA.StructMember sm = new org.omg.CORBA.StructMember("f",
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null);
        org.omg.CORBA.portable.OutputStream osSm = orb.create_output_stream();
        org.omg.CORBA.StructMemberHelper.write(osSm, sm);
        org.omg.CORBA.StructMember rtSm = org.omg.CORBA.StructMemberHelper.read(osSm.create_input_stream());
        check("f".equals(rtSm.name) && rtSm.type.kind() == org.omg.CORBA.TCKind.tk_long,
              "write/read CDR round-trip (name + nested TypeCode)");
        org.omg.CORBA.Any anySm = orb.create_any();
        org.omg.CORBA.StructMemberHelper.insert(anySm, sm);
        check("f".equals(org.omg.CORBA.StructMemberHelper.extract(anySm).name),
              "insert/extract Any round-trip keeps name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
