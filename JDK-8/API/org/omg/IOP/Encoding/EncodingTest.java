/*
 * @test
 * @summary org.omg.IOP.Encoding conformance: value type fields and constructors.
 * @run main EncodingTest
 */

public class EncodingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.Encoding s0 = new org.omg.IOP.Encoding();
        check(s0.format == (short)0, "no-arg format default");
        check(s0.major_version == (byte)0, "no-arg major_version default");
        check(s0.minor_version == (byte)0, "no-arg minor_version default");
        org.omg.IOP.Encoding s1 = new org.omg.IOP.Encoding((short)7, (byte)7, (byte)7);
        check(s1.format == (short)7, "ctor format stored");
        check(s1.major_version == (byte)7, "ctor major_version stored");
        check(s1.minor_version == (byte)7, "ctor minor_version stored");
        // ---- deepened (jdk8u372-b07 probed): value type full contract ----
        org.omg.IOP.Encoding e0 = new org.omg.IOP.Encoding();
        check(e0.format == (short) 0 && e0.major_version == (byte) 0 && e0.minor_version == (byte) 0,
              "no-arg ctor: (0,0,0) defaults");
        org.omg.IOP.Encoding e1 = new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2);
        check(e1.format == (short) 0 && e1.major_version == 1 && e1.minor_version == 2,
              "(short,byte,byte) ctor stores all three fields");
        e1.major_version = (byte) 3;
        check(e1.major_version == 3, "fields are mutable and read back");
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.Encoding.class.getModifiers()), "class is final");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.Encoding.class),
              "implements IDLEntity");
        check(org.omg.IOP.Encoding.class.getDeclaredFields().length == 3, "exactly three public fields");
        // the only encoding tuple the offline CodecFactory accepts
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        final org.omg.IOP.CodecFactory cf = org.omg.IOP.CodecFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("CodecFactory"));
        check(cf != null && cf.create_codec(new org.omg.IOP.Encoding((short) 0, (byte) 1, (byte) 2)) != null,
              "Encoding(0,1,2) accepted by offline CodecFactory");
        check(cf.create_codec(new org.omg.IOP.Encoding((short) 0, (byte) 1, (byte) 1)) != null,
              "Encoding(0,1,1) (GIOP 1.1 codec) accepted");
        boolean unknown1 = false, unknown2 = false;
        try { cf.create_codec(new org.omg.IOP.Encoding((short) 1, (byte) 1, (byte) 2)); }
        catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding u) { unknown1 = true; }
        try { cf.create_codec(new org.omg.IOP.Encoding((short) 0, (byte) 9, (byte) 9)); }
        catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding u) { unknown2 = true; }
        check(unknown1, "format 1 rejected -> UnknownEncoding");
        check(unknown2, "unsupported GIOP version (9,9) also rejected -> UnknownEncoding (versions are validated)");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
