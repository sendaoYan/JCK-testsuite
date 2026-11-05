/*
 * @test
 * @summary org.omg.IOP.Codec conformance: interface shape.
 * @run main CodecTest
 */

public class CodecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.Codec");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.IOP.CodecOperations.class.isAssignableFrom(c), "extends org.omg.IOP.CodecOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): offline encode/decode real closed loop ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        final org.omg.IOP.CodecFactory cf = org.omg.IOP.CodecFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("CodecFactory"));
        final org.omg.IOP.Codec codec = cf.create_codec(
            new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2));
        check(codec instanceof org.omg.IOP.Codec, "codec instance of Codec");
        // long closed loop: encode -> bytes -> decode -> extract_long
        org.omg.CORBA.Any la = orb.create_any();
        la.insert_long(4242);
        byte[] enc = codec.encode(la);
        check(enc.length == 12, "encode(Any(tk_long,4242)) is 12 bytes: 1 endianness + 4 TC + 4 value + padding");
        check(enc[0] == 0, "encapsulation first octet 0 = big-endian");
        org.omg.CORBA.Any back = codec.decode(enc);
        check(back.type().kind() == org.omg.CORBA.TCKind.tk_long && back.extract_long() == 4242,
              "decode(encode(x)) == x for long 4242");
        // string loop
        org.omg.CORBA.Any sa = orb.create_any();
        sa.insert_string("hello");
        check(codec.decode(codec.encode(sa)).extract_string().equals("hello"), "string encode/decode round-trip");
        // encode_value / decode_value
        byte[] ev = codec.encode_value(la);
        check(ev.length == 8, "encode_value omits the TypeCode: 8 bytes (endianness + 4 value + pad)");
        org.omg.CORBA.Any dv = codec.decode_value(ev, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        check(dv.extract_long() == 4242, "decode_value(encode_value(x), tk_long) == x");
        // negative value loop
        org.omg.CORBA.Any na = orb.create_any();
        na.insert_long(-7);
        check(codec.decode(codec.encode(na)).extract_long() == -7, "negative long round-trip");
        // ---- negative matrix (exact exception types, jdk8u372 probed) ----
        boolean badParam1 = false;
        try { codec.encode(null); } catch (org.omg.CORBA.BAD_PARAM b) { badParam1 = true; }
        check(badParam1, "encode(null) -> org.omg.CORBA.BAD_PARAM (SUN minor 201), NOT InvalidTypeForEncoding");
        boolean badParam2 = false;
        try { codec.encode_value(null); } catch (org.omg.CORBA.BAD_PARAM b) { badParam2 = true; }
        check(badParam2, "encode_value(null) -> org.omg.CORBA.BAD_PARAM");
        boolean fm1 = false;
        try { codec.decode(new byte[0]); } catch (org.omg.IOP.CodecPackage.FormatMismatch f) { fm1 = true; }
        check(fm1, "decode(empty byte[]) -> org.omg.IOP.CodecPackage.FormatMismatch");
        boolean fm2 = false;
        try { codec.decode(new byte[]{1, 2, 3}); } catch (org.omg.IOP.CodecPackage.FormatMismatch f) { fm2 = true; }
        check(fm2, "decode(too-short byte[3]) -> FormatMismatch");
        boolean badParam3 = false;
        try { codec.decode(null); } catch (org.omg.CORBA.BAD_PARAM b) { badParam3 = true; }
        check(badParam3, "decode(null) -> org.omg.CORBA.BAD_PARAM");
        boolean fm3 = false;
        try { codec.decode_value(new byte[]{1, 2, 3}, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long)); }
        catch (org.omg.IOP.CodecPackage.FormatMismatch f) { fm3 = true; }
        check(fm3, "decode_value(short bytes, tk_long) -> FormatMismatch");
        boolean fm4 = false;
        try {
            codec.decode_value(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                          (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f},
                               orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
        } catch (org.omg.IOP.CodecPackage.FormatMismatch f) { fm4 = true; }
        check(fm4, "decode_value(garbage bytes, tk_string) -> FormatMismatch (wraps internal MARSHAL)");
        // decode_value with mismatched primitive tc: jdk8 reinterprets silently, does NOT throw TypeMismatch
        org.omg.CORBA.Any wrong = codec.decode_value(enc, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
        check(wrong.type().kind() == org.omg.CORBA.TCKind.tk_string,
              "decode_value(long-bytes, tk_string) does not throw; returned Any carries the requested type");
        // null TypeCode for decode_value
        boolean badParam4 = false;
        try { codec.decode_value(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, null); }
        catch (org.omg.CORBA.BAD_PARAM b) { badParam4 = true; } catch (org.omg.IOP.CodecPackage.FormatMismatch f) { badParam4 = false; }
        check(badParam4, "decode_value(bytes, null TypeCode) -> org.omg.CORBA.BAD_PARAM");
        // method face
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.IOP.CodecOperations.class.getDeclaredMethods()) names.add(m.getName());
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                  "encode", "decode", "encode_value", "decode_value"))),
              "CodecOperations declares exactly encode/decode/encode_value/decode_value");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
