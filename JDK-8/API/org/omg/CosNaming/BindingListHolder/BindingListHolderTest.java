/*
 * @test
 * @summary org.omg.CosNaming.BindingListHolder conformance: holder value field.
 * @run main BindingListHolderTest
 */

public class BindingListHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingListHolder h0 = new org.omg.CosNaming.BindingListHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.Binding[] v = null;
        org.omg.CosNaming.BindingListHolder h1 = new org.omg.CosNaming.BindingListHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): default/ctor + _type kind + wire round-trip ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.BindingListHolder().value == null, "default ctor value == null");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.BindingListHolder.class), "implements Streamable");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.BindingListHolder.class.getModifiers()), "holder class is final");
        org.omg.CosNaming.Binding[] dpVal = new org.omg.CosNaming.Binding[]{new org.omg.CosNaming.Binding(new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("l", "m")}, org.omg.CosNaming.BindingType.ncontext)};
        org.omg.CosNaming.BindingListHolder dpH1 = new org.omg.CosNaming.BindingListHolder(dpVal);
        check(dpH1.value == dpVal, "one-arg ctor stores reference verbatim");
        check(new org.omg.CosNaming.BindingListHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias, "_type().kind() precise");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpH1._write(dpOs);
        org.omg.CosNaming.BindingListHolder dpHr = new org.omg.CosNaming.BindingListHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value != null && dpHr.value.length == 1 && dpHr.value[0].binding_name[0].id.equals("l") && dpHr.value[0].binding_type == org.omg.CosNaming.BindingType.ncontext, "_write/_read array round-trip element-wise");
        org.omg.CORBA.portable.OutputStream dpOs2 = orb.create_output_stream();
        org.omg.CosNaming.BindingListHelper.write(dpOs2, new org.omg.CosNaming.Binding[]{new org.omg.CosNaming.Binding(new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("l", "m")}, org.omg.CosNaming.BindingType.ncontext)});
        org.omg.CosNaming.BindingListHolder dpHr2 = new org.omg.CosNaming.BindingListHolder();
        dpHr2._read(dpOs2.create_input_stream());
        check(dpHr2.value != null && dpHr2.value.length == 1 && dpHr2.value[0].binding_name[0].id.equals("l"), "holder._read over BindingListHelper stream");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
