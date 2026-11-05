/*
 * @test
 * @summary org.omg.CORBA.UnionMemberHelper conformance: helper static id/type/narrow.
 * @run main UnionMemberHelperTest
 */

public class UnionMemberHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UnionMemberHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.UnionMemberHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.UnionMemberHelper.id().equals("IDL:omg.org/CORBA/UnionMember:1.0"),
              "UnionMemberHelper.id() exact repository id");
        check(org.omg.CORBA.UnionMemberHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "UnionMemberHelper.type().kind() == tk_struct");
        org.omg.CORBA.Any label = orb.create_any();
        label.insert_long(3);
        org.omg.CORBA.UnionMember um = new org.omg.CORBA.UnionMember("u", label,
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string), null);
        org.omg.CORBA.portable.OutputStream osUm = orb.create_output_stream();
        org.omg.CORBA.UnionMemberHelper.write(osUm, um);
        org.omg.CORBA.UnionMember rtUm = org.omg.CORBA.UnionMemberHelper.read(osUm.create_input_stream());
        check("u".equals(rtUm.name) && rtUm.label.extract_long() == 3,
              "write/read CDR round-trip (name + any label)");
        org.omg.CORBA.Any anyUm = orb.create_any();
        org.omg.CORBA.UnionMemberHelper.insert(anyUm, um);
        check("u".equals(org.omg.CORBA.UnionMemberHelper.extract(anyUm).name),
              "insert/extract Any round-trip keeps name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
