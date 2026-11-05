/*
 * @test
 * @summary org.omg.CosNaming.BindingTypeHolder conformance: holder value field.
 * @run main BindingTypeHolderTest
 */

public class BindingTypeHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingTypeHolder h0 = new org.omg.CosNaming.BindingTypeHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.BindingType v = null;
        org.omg.CosNaming.BindingTypeHolder h1 = new org.omg.CosNaming.BindingTypeHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): enum holder wire channel ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.BindingTypeHolder().value == null, "default ctor value == null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.BindingTypeHolder.class.getModifiers()), "holder final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.BindingTypeHolder.class), "implements Streamable");
        org.omg.CosNaming.BindingTypeHolder hn = new org.omg.CosNaming.BindingTypeHolder(org.omg.CosNaming.BindingType.nobject);
        check(hn.value == org.omg.CosNaming.BindingType.nobject, "one-arg ctor stores constant");
        check(new org.omg.CosNaming.BindingTypeHolder()._type().kind() == org.omg.CORBA.TCKind.tk_enum, "_type().kind() == tk_enum");
        check(new org.omg.CosNaming.BindingTypeHolder()._type().equal(org.omg.CosNaming.BindingTypeHelper.type()),
              "_type().equal(BindingTypeHelper.type())");
        org.omg.CORBA.portable.OutputStream osN = orb.create_output_stream();
        hn._write(osN);
        org.omg.CosNaming.BindingTypeHolder hrN = new org.omg.CosNaming.BindingTypeHolder();
        hrN._read(osN.create_input_stream());
        check(hrN.value == org.omg.CosNaming.BindingType.nobject, "_write/_read round-trip returns SAME enum constant");
        org.omg.CORBA.portable.OutputStream osC = orb.create_output_stream();
        new org.omg.CosNaming.BindingTypeHolder(org.omg.CosNaming.BindingType.ncontext)._write(osC);
        org.omg.CosNaming.BindingTypeHolder hrC = new org.omg.CosNaming.BindingTypeHolder();
        hrC._read(osC.create_input_stream());
        check(hrC.value == org.omg.CosNaming.BindingType.ncontext, "ncontext constant wire round-trip");
        org.omg.CosNaming.BindingTypeHolder mut = new org.omg.CosNaming.BindingTypeHolder();
        mut.value = org.omg.CosNaming.BindingType.ncontext;
        check(mut.value == org.omg.CosNaming.BindingType.ncontext, "value field mutable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
