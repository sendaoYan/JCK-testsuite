/*
 * @test
 * @summary org.omg.CORBA.StructMember conformance: value type fields and constructors.
 * @run main StructMemberTest
 */

public class StructMemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.StructMember s0 = new org.omg.CORBA.StructMember();
        check(s0.name == null, "no-arg name default");
        check(s0.type == null, "no-arg type default");
        check(s0.type_def == null, "no-arg type_def default");
        org.omg.CORBA.StructMember s1 = new org.omg.CORBA.StructMember("s", null, null);
        check(s1.name == "s", "ctor name stored");

        // ---- deepened: full ctor with a real TypeCode, field mutability, IDLEntity ----
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.TypeCode tc = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
        org.omg.CORBA.StructMember full = new org.omg.CORBA.StructMember("m", tc, null);
        check("m".equals(full.name), "full ctor name readback");
        check(full.type == tc, "full ctor keeps same TypeCode reference");
        check(full.type.equal(tc), "member type equal to primitive tc");
        check(full.type_def == null, "full ctor accepts null type_def (IDLType)");
        full.name = "renamed";
        check("renamed".equals(full.name), "name field mutable");
        full.type = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string);
        check(full.type.kind() == org.omg.CORBA.TCKind.tk_string, "type field mutable");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.StructMember.class),
              "StructMember implements IDLEntity");
        check(java.lang.reflect.Modifier.isPublic(org.omg.CORBA.StructMember.class.getModifiers())
              && java.lang.reflect.Modifier.isFinal(org.omg.CORBA.StructMember.class.getModifiers()),
              "StructMember is a public final value class (jdk8u372)");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.UnionMember.class.getModifiers())
              && java.lang.reflect.Modifier.isFinal(org.omg.CORBA.ValueMember.class.getModifiers()),
              "UnionMember and ValueMember are final too");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
