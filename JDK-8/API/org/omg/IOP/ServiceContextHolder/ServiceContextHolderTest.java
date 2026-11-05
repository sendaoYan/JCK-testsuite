/*
 * @test
 * @summary org.omg.IOP.ServiceContextHolder conformance: holder value field.
 * @run main ServiceContextHolderTest
 */

public class ServiceContextHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.ServiceContextHolder h0 = new org.omg.IOP.ServiceContextHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.ServiceContext v = null;
        org.omg.IOP.ServiceContextHolder h1 = new org.omg.IOP.ServiceContextHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): holder wire contract ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.ServiceContextHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.IOP.ServiceContextHolder.class),
              "implements Streamable");
        check(new org.omg.IOP.ServiceContextHolder().value == null, "no-arg default null");
        org.omg.IOP.ServiceContextHolder h = new org.omg.IOP.ServiceContextHolder(
            new org.omg.IOP.ServiceContext(7, new byte[]{3}));
        check(h.value.context_id == 7, "ctor stores value");
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        h._write(os);
        org.omg.IOP.ServiceContextHolder hr = new org.omg.IOP.ServiceContextHolder();
        hr._read(os.create_input_stream());
        check(hr.value.context_id == 7 && hr.value.context_data[0] == 3, "_write/_read CDR round-trip");
        check(new org.omg.IOP.ServiceContextHolder()._type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "_type().kind() == tk_struct");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
