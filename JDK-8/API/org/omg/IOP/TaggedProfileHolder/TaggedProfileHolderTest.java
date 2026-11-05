/*
 * @test
 * @summary org.omg.IOP.TaggedProfileHolder conformance: holder value field.
 * @run main TaggedProfileHolderTest
 */

public class TaggedProfileHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedProfileHolder h0 = new org.omg.IOP.TaggedProfileHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.TaggedProfile v = null;
        org.omg.IOP.TaggedProfileHolder h1 = new org.omg.IOP.TaggedProfileHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): holder wire contract ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.TaggedProfileHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.IOP.TaggedProfileHolder.class),
              "implements Streamable");
        check(new org.omg.IOP.TaggedProfileHolder().value == null, "no-arg default null");
        org.omg.IOP.TaggedProfileHolder h = new org.omg.IOP.TaggedProfileHolder(
            new org.omg.IOP.TaggedProfile(9, new byte[]{1}));
        check(h.value.tag == 9, "ctor stores value");
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        h._write(os);
        org.omg.IOP.TaggedProfileHolder hr = new org.omg.IOP.TaggedProfileHolder();
        hr._read(os.create_input_stream());
        check(hr.value.tag == 9 && hr.value.profile_data[0] == 1, "_write/_read CDR round-trip");
        check(new org.omg.IOP.TaggedProfileHolder()._type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "_type().kind() == tk_struct");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
