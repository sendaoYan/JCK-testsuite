/*
 * @test
 * @summary org.omg.CosNaming.NameComponentHolder conformance: holder value field.
 * @run main NameComponentHolderTest
 */

public class NameComponentHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NameComponentHolder h0 = new org.omg.CosNaming.NameComponentHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NameComponent v = null;
        org.omg.CosNaming.NameComponentHolder h1 = new org.omg.CosNaming.NameComponentHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): default/ctor + _type kind + wire round-trip ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.NameComponentHolder().value == null, "default ctor value == null");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.NameComponentHolder.class), "implements Streamable");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NameComponentHolder.class.getModifiers()), "holder class is final");
        org.omg.CosNaming.NameComponent dpVal = new org.omg.CosNaming.NameComponent("hid", "hkind");
        org.omg.CosNaming.NameComponentHolder dpH1 = new org.omg.CosNaming.NameComponentHolder(dpVal);
        check(dpH1.value == dpVal, "one-arg ctor stores reference verbatim");
        check(new org.omg.CosNaming.NameComponentHolder()._type().kind() == org.omg.CORBA.TCKind.tk_struct, "_type().kind() precise");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpH1._write(dpOs);
        org.omg.CosNaming.NameComponentHolder dpHr = new org.omg.CosNaming.NameComponentHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value != null && dpHr.value.id.equals("hid") && dpHr.value.kind.equals("hkind"), "_write/_read CDR round-trip field-wise");
        org.omg.CORBA.portable.OutputStream dpOs2 = orb.create_output_stream();
        org.omg.CosNaming.NameComponentHelper.write(dpOs2, new org.omg.CosNaming.NameComponent("hid", "hkind"));
        org.omg.CosNaming.NameComponentHolder dpHr2 = new org.omg.CosNaming.NameComponentHolder();
        dpHr2._read(dpOs2.create_input_stream());
        check(dpHr2.value != null && dpHr2.value.id.equals("hid") && dpHr2.value.kind.equals("hkind"), "holder._read over Helper-written stream field-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
