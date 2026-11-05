/*
 * @test
 * @summary org.omg.IOP.MultipleComponentProfileHolder conformance: holder value field.
 * @run main MultipleComponentProfileHolderTest
 */

public class MultipleComponentProfileHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.MultipleComponentProfileHolder h0 = new org.omg.IOP.MultipleComponentProfileHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.TaggedComponent[] v = null;
        org.omg.IOP.MultipleComponentProfileHolder h1 = new org.omg.IOP.MultipleComponentProfileHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): holder wire contract ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.MultipleComponentProfileHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.IOP.MultipleComponentProfileHolder.class),
              "implements Streamable");
        check(new org.omg.IOP.MultipleComponentProfileHolder().value == null, "no-arg default null");
        org.omg.IOP.TaggedComponent[] mcp = {new org.omg.IOP.TaggedComponent(25, new byte[]{1})};
        org.omg.IOP.MultipleComponentProfileHolder h = new org.omg.IOP.MultipleComponentProfileHolder(mcp);
        check(h.value.length == 1 && h.value[0].tag == 25, "ctor stores value");
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        h._write(os);
        org.omg.IOP.MultipleComponentProfileHolder hr = new org.omg.IOP.MultipleComponentProfileHolder();
        hr._read(os.create_input_stream());
        check(hr.value.length == 1 && hr.value[0].component_data[0] == 1, "_write/_read CDR round-trip");
        check(new org.omg.IOP.MultipleComponentProfileHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "_type().kind() == tk_alias");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
