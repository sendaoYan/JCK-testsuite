/*
 * @test
 * @summary org.omg.CosNaming.BindingIteratorHolder conformance: holder value field.
 * @run main BindingIteratorHolderTest
 */

public class BindingIteratorHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingIteratorHolder h0 = new org.omg.CosNaming.BindingIteratorHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.BindingIterator v = null;
        org.omg.CosNaming.BindingIteratorHolder h1 = new org.omg.CosNaming.BindingIteratorHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): objref holder, nil wire channel ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.BindingIteratorHolder().value == null, "default ctor value == null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.BindingIteratorHolder.class.getModifiers()), "holder final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.BindingIteratorHolder.class), "implements Streamable");
        check(new org.omg.CosNaming.BindingIteratorHolder()._type().kind() == org.omg.CORBA.TCKind.tk_objref, "_type().kind() == tk_objref");
        check(new org.omg.CosNaming.BindingIteratorHolder()._type().id().equals("IDL:omg.org/CosNaming/BindingIterator:1.0"), "_type().id() exact repository id");
        org.omg.CosNaming.BindingIteratorHolder dpHv = new org.omg.CosNaming.BindingIteratorHolder(null);
        check(dpHv.value == null, "one-arg ctor accepts null");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpHv._write(dpOs);
        org.omg.CosNaming.BindingIteratorHolder dpHr = new org.omg.CosNaming.BindingIteratorHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value == null, "nil reference _write/_read round-trip yields null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
