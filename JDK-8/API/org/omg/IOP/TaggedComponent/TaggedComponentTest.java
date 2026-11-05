/*
 * @test
 * @summary org.omg.IOP.TaggedComponent conformance: value type fields and constructors.
 * @run main TaggedComponentTest
 */

public class TaggedComponentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedComponent s0 = new org.omg.IOP.TaggedComponent();
        check(s0.tag == 0, "no-arg tag default");
        check(s0.component_data == null, "no-arg component_data default");
        org.omg.IOP.TaggedComponent s1 = new org.omg.IOP.TaggedComponent(7, null);
        check(s1.tag == 7, "ctor tag stored");
        // ---- deepened (jdk8u372-b07 probed): aliasing / defensive-copy probe ----
        // JDK-8 generated IDL value classes do NOT defensively copy: the field aliases the passed array.
        byte[] srcD = {1, 2, 3};
        org.omg.IOP.TaggedComponent alias = new org.omg.IOP.TaggedComponent(5, srcD);
        srcD[0] = 99;
        check(alias.component_data[0] == 99,
              "ctor stores the array reference without copy (mutating source is visible through field)");
        byte[] got1 = alias.component_data;
        got1[1] = 77;
        check(alias.component_data[1] == 77,
              "field read hands out the internal array (mutating the read array is visible)");
        check(alias.component_data == srcD, "component_data is reference-identical to the ctor argument");
        // wire round-trip DOES copy (marshaling materializes fresh octets)
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.TaggedComponentHelper.write(os, new org.omg.IOP.TaggedComponent(0xABCD, new byte[]{7, 8}));
        org.omg.IOP.TaggedComponent rt = org.omg.IOP.TaggedComponentHelper.read(os.create_input_stream());
        check(rt.tag == 0xABCD && rt.component_data.length == 2 && rt.component_data[0] == 7 && rt.component_data[1] == 8,
              "CDR wire round-trip tag+data");
        check(rt.component_data != srcD, "wire round-trip allocates a fresh array (no aliasing through the stream)");
        // class shape
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.TaggedComponent.class.getModifiers()), "class is final");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.TaggedComponent.class),
              "implements org.omg.CORBA.portable.IDLEntity");
        check(org.omg.IOP.TaggedComponent.class.getDeclaredFields().length == 2, "exactly two public fields (tag, component_data)");
        boolean npe = false;
        try {
            org.omg.CORBA.portable.OutputStream osN = orb.create_output_stream();
            org.omg.IOP.TaggedComponentHelper.write(osN, new org.omg.IOP.TaggedComponent(1, null));
        } catch (NullPointerException npeX) { npe = true; }
        check(npe, "TaggedComponentHelper.write with null component_data -> NullPointerException");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
