/*
 * @test
 * @summary org.omg.IOP.ENCODING_CDR_ENCAPS conformance: constant holder interface.
 * @run main ENCODING_CDR_ENCAPSTest
 */

public class ENCODING_CDR_ENCAPSTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.ENCODING_CDR_ENCAPS");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        // ---- deepened (jdk8u372-b07 probed): EncodingFormat constant exact contract ----
        check(org.omg.IOP.ENCODING_CDR_ENCAPS.value == (short) 0, "ENCODING_CDR_ENCAPS.value == 0");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == short.class, "value is short (EncodingFormat)");
        // only encoding format materialized in jdk8u372 rt.jar; offline CodecFactory accepts only this one
        final org.omg.CORBA.ORB orb0 = org.omg.CORBA.ORB.init(new String[0], null);
        final org.omg.IOP.CodecFactory cf0 = org.omg.IOP.CodecFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb0.resolve_initial_references("CodecFactory"));
        org.omg.IOP.Codec okCodec = cf0.create_codec(
            new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2));
        check(okCodec != null, "create_codec(ENCODING_CDR_ENCAPS,1,2) succeeds offline");
        boolean unknownEnc = false;
        try { cf0.create_codec(new org.omg.IOP.Encoding((short) 77, (byte) 1, (byte) 2)); }
        catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding ue) { unknownEnc = true; }
        check(unknownEnc, "create_codec(format 77) -> org.omg.IOP.CodecFactoryPackage.UnknownEncoding");
        orb0.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
