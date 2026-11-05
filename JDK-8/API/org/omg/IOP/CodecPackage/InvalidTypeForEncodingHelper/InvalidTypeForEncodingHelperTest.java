/*
 * @test
 * @summary org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper conformance: helper static id/type/narrow.
 * @run main InvalidTypeForEncodingHelperTest
 */

public class InvalidTypeForEncodingHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.id() != null, "id() non-null");
        check(org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): helper wire/Any/type contract ----
        check(org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.id().equals("IDL:omg.org/IOP/Codec/InvalidTypeForEncoding:1.0"), "id() exact");
        check(org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "type().kind() == tk_except");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.write(os, new org.omg.IOP.CodecPackage.InvalidTypeForEncoding("m"));
        org.omg.IOP.CodecPackage.InvalidTypeForEncoding rt = org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.read(os.create_input_stream());
        check(rt != null && rt.getClass() == org.omg.IOP.CodecPackage.InvalidTypeForEncoding.class, "write/read round-trip class preserved");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.insert(a, new org.omg.IOP.CodecPackage.InvalidTypeForEncoding("n"));
        check(org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.extract(a) != null, "insert/extract Any round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type after insert is tk_except");
        // two back-to-back in one stream
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.write(os2, new org.omg.IOP.CodecPackage.InvalidTypeForEncoding());
        org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.write(os2, new org.omg.IOP.CodecPackage.InvalidTypeForEncoding());
        org.omg.CORBA.portable.InputStream is2 = os2.create_input_stream();
        check(org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.read(is2) != null && org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper.read(is2) != null, "two exceptions back-to-back in one stream");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
