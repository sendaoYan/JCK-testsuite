/*
 * @test
 * @summary org.omg.IOP.TaggedProfile conformance: value type fields and constructors.
 * @run main TaggedProfileTest
 */

public class TaggedProfileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedProfile s0 = new org.omg.IOP.TaggedProfile();
        check(s0.tag == 0, "no-arg tag default");
        check(s0.profile_data == null, "no-arg profile_data default");
        org.omg.IOP.TaggedProfile s1 = new org.omg.IOP.TaggedProfile(7, null);
        check(s1.tag == 7, "ctor tag stored");
        // ---- deepened (jdk8u372-b07 probed): aliasing / defensive-copy probe ----
        byte[] srcD = {9};
        org.omg.IOP.TaggedProfile alias = new org.omg.IOP.TaggedProfile(7, srcD);
        srcD[0] = 88;
        check(alias.profile_data[0] == 88, "ctor stores array reference without copy (source mutation visible)");
        check(alias.profile_data == srcD, "profile_data is reference-identical to the ctor argument");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.TaggedProfileHelper.write(os, new org.omg.IOP.TaggedProfile(1, new byte[]{3, 4, 5}));
        org.omg.IOP.TaggedProfile rt = org.omg.IOP.TaggedProfileHelper.read(os.create_input_stream());
        check(rt.tag == 1 && rt.profile_data.length == 3 && rt.profile_data[2] == 5, "CDR wire round-trip tag+data");
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.TaggedProfile.class.getModifiers()), "class is final");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.TaggedProfile.class),
              "implements IDLEntity");
        check(org.omg.IOP.TaggedProfile.class.getDeclaredFields().length == 2, "exactly two public fields (tag, profile_data)");
        boolean npe = false;
        try {
            org.omg.CORBA.portable.OutputStream osN = orb.create_output_stream();
            org.omg.IOP.TaggedProfileHelper.write(osN, new org.omg.IOP.TaggedProfile(1, null));
        } catch (NullPointerException npeX) { npe = true; }
        check(npe, "TaggedProfileHelper.write with null profile_data -> NullPointerException");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
