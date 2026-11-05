/*
 * @test
 * @summary org.omg.CosNaming.NamingContextHolder conformance: holder value field.
 * @run main NamingContextHolderTest
 */

public class NamingContextHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextHolder h0 = new org.omg.CosNaming.NamingContextHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContext v = null;
        org.omg.CosNaming.NamingContextHolder h1 = new org.omg.CosNaming.NamingContextHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): objref holder, nil wire channel ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.NamingContextHolder().value == null, "default ctor value == null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextHolder.class.getModifiers()), "holder final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.NamingContextHolder.class), "implements Streamable");
        check(new org.omg.CosNaming.NamingContextHolder()._type().kind() == org.omg.CORBA.TCKind.tk_objref, "_type().kind() == tk_objref");
        check(new org.omg.CosNaming.NamingContextHolder()._type().id().equals("IDL:omg.org/CosNaming/NamingContext:1.0"), "_type().id() exact repository id");
        org.omg.CosNaming.NamingContextHolder dpHv = new org.omg.CosNaming.NamingContextHolder(null);
        check(dpHv.value == null, "one-arg ctor accepts null");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpHv._write(dpOs);
        org.omg.CosNaming.NamingContextHolder dpHr = new org.omg.CosNaming.NamingContextHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value == null, "nil reference _write/_read round-trip yields null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
