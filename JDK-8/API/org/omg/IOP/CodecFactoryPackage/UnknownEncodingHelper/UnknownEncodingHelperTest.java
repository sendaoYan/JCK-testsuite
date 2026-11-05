/*
 * @test
 * @summary org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper conformance: helper static id/type/narrow.
 * @run main UnknownEncodingHelperTest
 */

public class UnknownEncodingHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.id() != null, "id() non-null");
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): helper wire/Any/type contract ----
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.id().equals("IDL:omg.org/IOP/CodecFactory/UnknownEncoding:1.0"), "id() exact");
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "type().kind() == tk_except");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.write(os, new org.omg.IOP.CodecFactoryPackage.UnknownEncoding("m"));
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding rt = org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.read(os.create_input_stream());
        check(rt != null && rt.getClass() == org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class, "write/read round-trip class preserved");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.insert(a, new org.omg.IOP.CodecFactoryPackage.UnknownEncoding("n"));
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.extract(a) != null, "insert/extract Any round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type after insert is tk_except");
        // two back-to-back in one stream
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.write(os2, new org.omg.IOP.CodecFactoryPackage.UnknownEncoding());
        org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.write(os2, new org.omg.IOP.CodecFactoryPackage.UnknownEncoding());
        org.omg.CORBA.portable.InputStream is2 = os2.create_input_stream();
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.read(is2) != null && org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.read(is2) != null, "two exceptions back-to-back in one stream");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
