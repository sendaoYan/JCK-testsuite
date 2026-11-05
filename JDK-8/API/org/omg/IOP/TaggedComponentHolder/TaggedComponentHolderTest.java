/*
 * @test
 * @summary org.omg.IOP.TaggedComponentHolder conformance: holder value field.
 * @run main TaggedComponentHolderTest
 */

public class TaggedComponentHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedComponentHolder h0 = new org.omg.IOP.TaggedComponentHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.TaggedComponent v = null;
        org.omg.IOP.TaggedComponentHolder h1 = new org.omg.IOP.TaggedComponentHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): holder wire contract ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.TaggedComponentHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.IOP.TaggedComponentHolder.class),
              "implements Streamable");
        check(new org.omg.IOP.TaggedComponentHolder().value == null, "no-arg default null");
        org.omg.IOP.TaggedComponentHolder h = new org.omg.IOP.TaggedComponentHolder(
            new org.omg.IOP.TaggedComponent(5, new byte[]{1}));
        check(h.value.tag == 5, "ctor stores value");
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        h._write(os);
        org.omg.IOP.TaggedComponentHolder hr = new org.omg.IOP.TaggedComponentHolder();
        hr._read(os.create_input_stream());
        check(hr.value.tag == 5 && hr.value.component_data.length == 1 && hr.value.component_data[0] == 1,
              "_write/_read CDR round-trip");
        check(new org.omg.IOP.TaggedComponentHolder()._type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "_type().kind() == tk_struct");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
