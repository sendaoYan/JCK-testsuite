/*
 * @test
 * @summary org.omg.CosNaming.NameHolder conformance: holder value field.
 * @run main NameHolderTest
 */

public class NameHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NameHolder h0 = new org.omg.CosNaming.NameHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NameComponent[] v = null;
        org.omg.CosNaming.NameHolder h1 = new org.omg.CosNaming.NameHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): default/ctor + _type kind + wire round-trip ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.NameHolder().value == null, "default ctor value == null");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.NameHolder.class), "implements Streamable");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NameHolder.class.getModifiers()), "holder class is final");
        org.omg.CosNaming.NameComponent[] dpVal = new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("n1", "k1"), new org.omg.CosNaming.NameComponent("n2", "k2")};
        org.omg.CosNaming.NameHolder dpH1 = new org.omg.CosNaming.NameHolder(dpVal);
        check(dpH1.value == dpVal, "one-arg ctor stores reference verbatim");
        check(new org.omg.CosNaming.NameHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias, "_type().kind() precise");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpH1._write(dpOs);
        org.omg.CosNaming.NameHolder dpHr = new org.omg.CosNaming.NameHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value != null && dpHr.value.length == 2 && dpHr.value[0].id.equals("n1") && dpHr.value[1].kind.equals("k2"), "_write/_read array round-trip element-wise");
        org.omg.CORBA.portable.OutputStream dpOs2 = orb.create_output_stream();
        org.omg.CosNaming.NameHelper.write(dpOs2, new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("n1", "k1"), new org.omg.CosNaming.NameComponent("n2", "k2")});
        org.omg.CosNaming.NameHolder dpHr2 = new org.omg.CosNaming.NameHolder();
        dpHr2._read(dpOs2.create_input_stream());
        check(dpHr2.value != null && dpHr2.value.length == 2 && dpHr2.value[1].id.equals("n2"), "holder._read over NameHelper stream");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
