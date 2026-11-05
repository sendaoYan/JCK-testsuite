/*
 * @test
 * @summary org.omg.CORBA.ValueMember conformance: value type fields and constructors.
 * @run main ValueMemberTest
 */

public class ValueMemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ValueMember s0 = new org.omg.CORBA.ValueMember();
        check(s0.name == null, "no-arg name default");
        check(s0.id == null, "no-arg id default");
        check(s0.defined_in == null, "no-arg defined_in default");
        check(s0.version == null, "no-arg version default");
        check(s0.type == null, "no-arg type default");
        check(s0.type_def == null, "no-arg type_def default");
        check(s0.access == (short)0, "no-arg access default");
        org.omg.CORBA.ValueMember s1 = new org.omg.CORBA.ValueMember("s", "s", "s", "s", null, null, (short)7);
        check(s1.name == "s", "ctor name stored");
        check(s1.id == "s", "ctor id stored");
        check(s1.defined_in == "s", "ctor defined_in stored");
        check(s1.version == "s", "ctor version stored");
        check(s1.access == (short)7, "ctor access stored");

        // ---- deepened: full ctor with real TypeCode, access constants, field mutation ----
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.TypeCode tc = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
        org.omg.CORBA.ValueMember full =
            new org.omg.CORBA.ValueMember("n", "IDL:X:1.0", "container", "2.0", tc, null,
                                          org.omg.CORBA.PUBLIC_MEMBER.value);
        check("n".equals(full.name), "full ctor name");
        check("IDL:X:1.0".equals(full.id), "full ctor id");
        check("container".equals(full.defined_in), "full ctor defined_in");
        check("2.0".equals(full.version), "full ctor version");
        check(full.type == tc, "full ctor keeps same TypeCode reference");
        check(full.type_def == null, "null type_def (IDLType) accepted");
        check(full.access == org.omg.CORBA.PUBLIC_MEMBER.value, "full ctor access PUBLIC_MEMBER(1)");
        full.access = org.omg.CORBA.PRIVATE_MEMBER.value;
        check(full.access == org.omg.CORBA.PRIVATE_MEMBER.value, "access field mutable to PRIVATE_MEMBER(0)");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.ValueMember.class),
              "ValueMember implements IDLEntity");
        check(org.omg.CORBA.PUBLIC_MEMBER.value == 1 && org.omg.CORBA.PRIVATE_MEMBER.value == 0,
              "visibility constants {0,1}");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
