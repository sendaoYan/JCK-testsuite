/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtHolder conformance: holder value field.
 * @run main NamingContextExtHolderTest
 */

public class NamingContextExtHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextExtHolder h0 = new org.omg.CosNaming.NamingContextExtHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextExt v = null;
        org.omg.CosNaming.NamingContextExtHolder h1 = new org.omg.CosNaming.NamingContextExtHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): objref holder, nil wire channel ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.NamingContextExtHolder().value == null, "default ctor value == null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextExtHolder.class.getModifiers()), "holder final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtHolder.class), "implements Streamable");
        check(new org.omg.CosNaming.NamingContextExtHolder()._type().kind() == org.omg.CORBA.TCKind.tk_objref, "_type().kind() == tk_objref");
        check(new org.omg.CosNaming.NamingContextExtHolder()._type().id().equals("IDL:omg.org/CosNaming/NamingContextExt:1.0"), "_type().id() exact repository id");
        org.omg.CosNaming.NamingContextExtHolder dpHv = new org.omg.CosNaming.NamingContextExtHolder(null);
        check(dpHv.value == null, "one-arg ctor accepts null");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpHv._write(dpOs);
        org.omg.CosNaming.NamingContextExtHolder dpHr = new org.omg.CosNaming.NamingContextExtHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value == null, "nil reference _write/_read round-trip yields null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
