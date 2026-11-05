/*
 * @test
 * @summary org.omg.IOP.IOR conformance: value type fields and constructors.
 * @run main IORTest
 */

public class IORTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.IOR s0 = new org.omg.IOP.IOR();
        check(s0.type_id == null, "no-arg type_id default");
        check(s0.profiles == null, "no-arg profiles default");
        org.omg.IOP.IOR s1 = new org.omg.IOP.IOR("s", null);
        check(s1.type_id == "s", "ctor type_id stored");
        check(s1.profiles == null, "full ctor with null profiles array stores null");
        // ---- deepened (jdk8u372-b07 probed): IOR full contract ----
        org.omg.IOP.TaggedProfile[] srcP = {
            new org.omg.IOP.TaggedProfile(org.omg.IOP.TAG_INTERNET_IOP.value, new byte[]{0, 1, 2, 3, 4}),
            new org.omg.IOP.TaggedProfile(org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value, new byte[]{5})};
        org.omg.IOP.IOR full = new org.omg.IOP.IOR("IDL:Demo:1.0", srcP);
        check(full.type_id.equals("IDL:Demo:1.0"), "full ctor stores type_id");
        check(full.profiles == srcP, "profiles array stored by reference (no defensive copy of the array)");
        check(full.profiles.length == 2 && full.profiles[0].tag == 0 && full.profiles[1].tag == 1,
              "profiles elements readable with exact tags");
        srcP[0] = null;
        check(full.profiles[0] == null, "mutating the source array is visible (aliasing, no snapshot)");
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.IOR.class.getModifiers()), "class is final");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.IOR.class), "implements IDLEntity");
        // CDR wire round-trip (IORHelper.write -> read)
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.IOP.TaggedProfile[] ps = {
            new org.omg.IOP.TaggedProfile(org.omg.IOP.TAG_INTERNET_IOP.value, new byte[]{0, 1, 2, 3, 4}),
            new org.omg.IOP.TaggedProfile(org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value, new byte[]{5})};
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.IORHelper.write(os, new org.omg.IOP.IOR("IDL:Demo:1.0", ps));
        org.omg.IOP.IOR back = org.omg.IOP.IORHelper.read(os.create_input_stream());
        check(back.type_id.equals("IDL:Demo:1.0"), "wire round-trip type_id equals");
        check(back.profiles.length == 2, "wire round-trip profiles length 2");
        check(back.profiles[0].tag == 0 && back.profiles[0].profile_data.length == 5,
              "wire round-trip profile[0] tag+data length");
        check(back.profiles[1].tag == 1 && back.profiles[1].profile_data.length == 1 && back.profiles[1].profile_data[0] == 5,
              "wire round-trip profile[1] tag+data bytes");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.IOP.IORHelper.write(osE, new org.omg.IOP.IOR("X", new org.omg.IOP.TaggedProfile[0]));
        org.omg.IOP.IOR backE = org.omg.IOP.IORHelper.read(osE.create_input_stream());
        check(backE.type_id.equals("X") && backE.profiles.length == 0, "wire round-trip empty profile sequence");
        boolean npe = false;
        try { org.omg.IOP.IORHelper.write(orb.create_output_stream(), new org.omg.IOP.IOR("t", null)); }
        catch (NullPointerException npeX) { npe = true; }
        check(npe, "IORHelper.write with null profiles -> NullPointerException");
        // Any channel round-trip
        org.omg.CORBA.Any iorAny = orb.create_any();
        org.omg.IOP.IORHelper.insert(iorAny, new org.omg.IOP.IOR("IDL:AnyIOR:1.0", ps));
        org.omg.IOP.IOR backA = org.omg.IOP.IORHelper.extract(iorAny);
        check(backA.type_id.equals("IDL:AnyIOR:1.0") && backA.profiles.length == 2, "Any insert/extract round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
