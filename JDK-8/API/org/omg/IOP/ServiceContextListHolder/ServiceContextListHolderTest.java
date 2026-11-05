/*
 * @test
 * @summary org.omg.IOP.ServiceContextListHolder conformance: holder value field.
 * @run main ServiceContextListHolderTest
 */

public class ServiceContextListHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.ServiceContextListHolder h0 = new org.omg.IOP.ServiceContextListHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.ServiceContext[] v = null;
        org.omg.IOP.ServiceContextListHolder h1 = new org.omg.IOP.ServiceContextListHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): holder wire contract ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.ServiceContextListHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.IOP.ServiceContextListHolder.class),
              "implements Streamable");
        check(new org.omg.IOP.ServiceContextListHolder().value == null, "no-arg default null (array holder)");
        org.omg.IOP.ServiceContext[] scl = {new org.omg.IOP.ServiceContext(1, new byte[]{1})};
        org.omg.IOP.ServiceContextListHolder h = new org.omg.IOP.ServiceContextListHolder(scl);
        check(h.value.length == 1, "ctor stores value");
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        h._write(os);
        org.omg.IOP.ServiceContextListHolder hr = new org.omg.IOP.ServiceContextListHolder();
        hr._read(os.create_input_stream());
        check(hr.value.length == 1 && hr.value[0].context_id == 1, "_write/_read CDR round-trip");
        check(new org.omg.IOP.ServiceContextListHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "_type().kind() == tk_alias");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
