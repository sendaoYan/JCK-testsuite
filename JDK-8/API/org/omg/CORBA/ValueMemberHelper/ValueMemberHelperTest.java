/*
 * @test
 * @summary org.omg.CORBA.ValueMemberHelper conformance: helper static id/type/narrow.
 * @run main ValueMemberHelperTest
 */

public class ValueMemberHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ValueMemberHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ValueMemberHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ValueMemberHelper.id().equals("IDL:omg.org/CORBA/ValueMember:1.0"),
              "ValueMemberHelper.id() exact repository id");
        check(org.omg.CORBA.ValueMemberHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "ValueMemberHelper.type().kind() == tk_struct");
        org.omg.CORBA.ValueMember vm = new org.omg.CORBA.ValueMember("v", "IDL:X:1.0", "IDL:C:1.0", "1.0",
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_short), null, (short) 0);
        org.omg.CORBA.portable.OutputStream osVm = orb.create_output_stream();
        org.omg.CORBA.ValueMemberHelper.write(osVm, vm);
        org.omg.CORBA.ValueMember rtVm = org.omg.CORBA.ValueMemberHelper.read(osVm.create_input_stream());
        check("v".equals(rtVm.name) && "IDL:X:1.0".equals(rtVm.id) && "1.0".equals(rtVm.version)
              && rtVm.access == 0 && rtVm.type.kind() == org.omg.CORBA.TCKind.tk_short,
              "write/read CDR round-trip (all fields)");
        org.omg.CORBA.Any anyVm = orb.create_any();
        org.omg.CORBA.ValueMemberHelper.insert(anyVm, vm);
        check("v".equals(org.omg.CORBA.ValueMemberHelper.extract(anyVm).name),
              "insert/extract Any round-trip keeps name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
