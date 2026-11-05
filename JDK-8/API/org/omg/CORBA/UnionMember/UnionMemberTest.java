/*
 * @test
 * @summary org.omg.CORBA.UnionMember conformance: value type fields and constructors.
 * @run main UnionMemberTest
 */

public class UnionMemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.UnionMember s0 = new org.omg.CORBA.UnionMember();
        check(s0.name == null, "no-arg name default");
        check(s0.label == null, "no-arg label default");
        check(s0.type == null, "no-arg type default");
        check(s0.type_def == null, "no-arg type_def default");
        org.omg.CORBA.UnionMember s1 = new org.omg.CORBA.UnionMember("s", null, null, null);
        check(s1.name == "s", "ctor name stored");

        // ---- deepened: full ctor with real Any label + TypeCode, field mutability ----
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.Any label = orb.create_any();
        label.insert_long(7);
        org.omg.CORBA.TypeCode tc = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string);
        org.omg.CORBA.UnionMember full = new org.omg.CORBA.UnionMember("f", label, tc, null);
        check("f".equals(full.name), "full ctor name");
        check(full.label == label, "full ctor keeps same label Any reference");
        check(full.label.extract_long() == 7, "label value readable");
        check(full.type == tc, "full ctor keeps same TypeCode reference");
        check(full.type_def == null, "null type_def accepted");
        org.omg.CORBA.Any dflt = orb.create_any();
        full.label = dflt;
        check(full.label == dflt && full.label.type().kind() == org.omg.CORBA.TCKind.tk_null,
              "label field mutable, empty any marks default arm");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.UnionMember.class),
              "UnionMember implements IDLEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
