/*
 * @test
 * @summary org.omg.IOP.IORHolder conformance: holder value field.
 * @run main IORHolderTest
 */

public class IORHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.IORHolder h0 = new org.omg.IOP.IORHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.IOR v = null;
        org.omg.IOP.IORHolder h1 = new org.omg.IOP.IORHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): holder wire contract ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.IORHolder.class.getModifiers()), "class is final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.IOP.IORHolder.class),
              "implements org.omg.CORBA.portable.Streamable");
        org.omg.IOP.IORHolder hw0 = new org.omg.IOP.IORHolder();
        check(hw0.value == null, "no-arg default value null");
        org.omg.IOP.TaggedProfile[] ps = {
            new org.omg.IOP.TaggedProfile(0, new byte[]{1}),
            new org.omg.IOP.TaggedProfile(1, new byte[]{2, 3})};
        org.omg.IOP.IORHolder hw1 = new org.omg.IOP.IORHolder(new org.omg.IOP.IOR("IDL:H:1.0", ps));
        check(hw1.value.type_id.equals("IDL:H:1.0") && hw1.value.profiles.length == 2, "ctor stores value");
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        hw1._write(os);
        org.omg.IOP.IORHolder hr = new org.omg.IOP.IORHolder();
        hr._read(os.create_input_stream());
        check(hr.value.type_id.equals("IDL:H:1.0") && hr.value.profiles.length == 2
              && hr.value.profiles[1].profile_data[1] == 3, "_write/_read CDR round-trip");
        check(new org.omg.IOP.IORHolder()._type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "_type().kind() == tk_struct");
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        new org.omg.IOP.IORHolder(new org.omg.IOP.IOR("A", new org.omg.IOP.TaggedProfile[0]))._write(os2);
        new org.omg.IOP.IORHolder(new org.omg.IOP.IOR("B", new org.omg.IOP.TaggedProfile[0]))._write(os2);
        org.omg.IOP.IORHolder hA = new org.omg.IOP.IORHolder(), hB = new org.omg.IOP.IORHolder();
        org.omg.CORBA.portable.InputStream is2 = os2.create_input_stream();
        hA._read(is2); hB._read(is2);
        check(hA.value.type_id.equals("A") && hB.value.type_id.equals("B"),
              "two holders back-to-back in one stream");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
