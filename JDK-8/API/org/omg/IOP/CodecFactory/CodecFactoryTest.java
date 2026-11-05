/*
 * @test
 * @summary org.omg.IOP.CodecFactory conformance: interface shape.
 * @run main CodecFactoryTest
 */

public class CodecFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.CodecFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.IOP.CodecFactoryOperations.class.isAssignableFrom(c), "extends org.omg.IOP.CodecFactoryOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): offline factory closed loop ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        final org.omg.IOP.CodecFactory cf = org.omg.IOP.CodecFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("CodecFactory"));
        check(cf != null, "narrow(resolve_initial_references(CodecFactory)) non-null");
        org.omg.IOP.Codec c12 = cf.create_codec(
            new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2));
        org.omg.IOP.Codec c11 = cf.create_codec(
            new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 1));
        check(c12 != null && c11 != null, "create_codec for GIOP 1.2 and 1.1 both succeed");
        org.omg.CORBA.Any la = orb.create_any();
        la.insert_long(4242);
        check(c12.decode(c12.encode(la)).extract_long() == 4242, "GIOP 1.2 codec round-trip");
        check(c11.decode(c11.encode(la)).extract_long() == 4242, "GIOP 1.1 codec round-trip");
        boolean ue1 = false;
        try { cf.create_codec(new org.omg.IOP.Encoding((short) 77, (byte) 1, (byte) 2)); }
        catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding u) { ue1 = true; }
        check(ue1, "create_codec(format=77) -> org.omg.IOP.CodecFactoryPackage.UnknownEncoding");
        boolean ue2 = false;
        try { cf.create_codec(new org.omg.IOP.Encoding((short) 1, (byte) 1, (byte) 2)); }
        catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding u) { ue2 = true; }
        check(ue2, "create_codec(ENCODING_..=1 unsupported format) -> UnknownEncoding");
        boolean ue3 = false;
        try { cf.create_codec(new org.omg.IOP.Encoding((short) 0, (byte) 9, (byte) 9)); }
        catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding u) { ue3 = true; }
        check(ue3, "create_codec(0,9,9 unsupported GIOP version) -> UnknownEncoding");
        boolean ue4 = false;
        try { cf.create_codec(new org.omg.IOP.Encoding((short) -1, (byte) 1, (byte) 2)); }
        catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding u) { ue4 = true; }
        check(ue4, "create_codec(format=-1) -> UnknownEncoding");
        boolean badParam = false;
        try { cf.create_codec(null); } catch (org.omg.CORBA.BAD_PARAM b) { badParam = true; }
        check(badParam, "create_codec(null Encoding) -> org.omg.CORBA.BAD_PARAM (SUN minor 201)");
        // repeated codec creation yields independent usable codecs
        org.omg.IOP.Codec again = cf.create_codec(
            new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2));
        check(again.decode(again.encode(la)).extract_long() == 4242, "second factory-made codec also round-trips");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
