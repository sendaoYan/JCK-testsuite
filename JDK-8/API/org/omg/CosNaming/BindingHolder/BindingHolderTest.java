/*
 * @test
 * @summary org.omg.CosNaming.BindingHolder conformance: holder value field.
 * @run main BindingHolderTest
 */

public class BindingHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingHolder h0 = new org.omg.CosNaming.BindingHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.Binding v = null;
        org.omg.CosNaming.BindingHolder h1 = new org.omg.CosNaming.BindingHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): default/ctor + _type kind + wire round-trip ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.BindingHolder().value == null, "default ctor value == null");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.BindingHolder.class), "implements Streamable");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.BindingHolder.class.getModifiers()), "holder class is final");
        org.omg.CosNaming.Binding dpVal = new org.omg.CosNaming.Binding(new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("b", "k")}, org.omg.CosNaming.BindingType.nobject);
        org.omg.CosNaming.BindingHolder dpH1 = new org.omg.CosNaming.BindingHolder(dpVal);
        check(dpH1.value == dpVal, "one-arg ctor stores reference verbatim");
        check(new org.omg.CosNaming.BindingHolder()._type().kind() == org.omg.CORBA.TCKind.tk_struct, "_type().kind() precise");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpH1._write(dpOs);
        org.omg.CosNaming.BindingHolder dpHr = new org.omg.CosNaming.BindingHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value != null && dpHr.value.binding_name.length == 1 && dpHr.value.binding_name[0].id.equals("b") && dpHr.value.binding_type == org.omg.CosNaming.BindingType.nobject, "_write/_read CDR round-trip field-wise");
        org.omg.CORBA.portable.OutputStream dpOs2 = orb.create_output_stream();
        org.omg.CosNaming.BindingHelper.write(dpOs2, new org.omg.CosNaming.Binding(new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("b", "k")}, org.omg.CosNaming.BindingType.nobject));
        org.omg.CosNaming.BindingHolder dpHr2 = new org.omg.CosNaming.BindingHolder();
        dpHr2._read(dpOs2.create_input_stream());
        check(dpHr2.value != null && dpHr2.value.binding_name.length == 1 && dpHr2.value.binding_name[0].id.equals("b"), "holder._read over Helper-written stream");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
