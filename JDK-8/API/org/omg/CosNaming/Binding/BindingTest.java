/*
 * @test
 * @summary org.omg.CosNaming.Binding conformance: value type fields and constructors.
 * @run main BindingTest
 */

public class BindingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.Binding s0 = new org.omg.CosNaming.Binding();
        check(s0.binding_name == null, "no-arg binding_name default");
        check(s0.binding_type == null, "no-arg binding_type default");
        org.omg.CosNaming.Binding s1 = new org.omg.CosNaming.Binding(null, null);
        // ---- deepened (batch C, jdk8u372 probed): identity semantics + field contract ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.Binding.class.getModifiers()),
              "Binding is final (IDL struct mapping)");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.Binding.class),
              "Binding implements IDLEntity");
        check(new org.omg.CosNaming.Binding().binding_name == null
              && new org.omg.CosNaming.Binding().binding_type == null,
              "default ctor leaves binding_name and binding_type null");
        org.omg.CosNaming.NameComponent[] shared = new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("n", "k")};
        org.omg.CosNaming.Binding b = new org.omg.CosNaming.Binding(shared, org.omg.CosNaming.BindingType.ncontext);
        check(b.binding_name == shared, "ctor stores the array reference itself (no defensive copy)");
        check(b.binding_name.length == 1, "binding_name length readback");
        check(b.binding_name[0].id.equals("n") && b.binding_name[0].kind.equals("k"), "binding_name[0] fields readback");
        check(b.binding_type == org.omg.CosNaming.BindingType.ncontext, "binding_type constant readback");
        check(b.equals(b), "equals identity: same instance true");
        check(!b.equals(new org.omg.CosNaming.Binding(shared, org.omg.CosNaming.BindingType.ncontext)),
              "equals is identity: structurally identical distinct instance NOT equal (no value-equals override)");
        check(!b.equals(null), "equals(null) false");
        check(b.hashCode() == System.identityHashCode(b), "hashCode not overridden");
        check(b.toString().startsWith("org.omg.CosNaming.Binding@"), "toString default Object pattern");
        org.omg.CosNaming.Binding mut = new org.omg.CosNaming.Binding();
        mut.binding_name = shared;
        mut.binding_type = org.omg.CosNaming.BindingType.nobject;
        check(mut.binding_type == org.omg.CosNaming.BindingType.nobject && mut.binding_name == shared, "fields mutable");
        org.omg.CosNaming.Binding emptyName = new org.omg.CosNaming.Binding(new org.omg.CosNaming.NameComponent[0], org.omg.CosNaming.BindingType.nobject);
        check(emptyName.binding_name.length == 0, "empty name array accepted, length 0");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
