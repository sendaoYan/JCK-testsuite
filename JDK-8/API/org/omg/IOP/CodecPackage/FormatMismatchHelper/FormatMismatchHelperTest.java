/*
 * @test
 * @summary org.omg.IOP.CodecPackage.FormatMismatchHelper conformance: helper static id/type/narrow.
 * @run main FormatMismatchHelperTest
 */

public class FormatMismatchHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.id() != null, "id() non-null");
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): helper wire/Any/type contract ----
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.id().equals("IDL:omg.org/IOP/Codec/FormatMismatch:1.0"), "id() exact");
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "type().kind() == tk_except");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.CodecPackage.FormatMismatchHelper.write(os, new org.omg.IOP.CodecPackage.FormatMismatch("m"));
        org.omg.IOP.CodecPackage.FormatMismatch rt = org.omg.IOP.CodecPackage.FormatMismatchHelper.read(os.create_input_stream());
        check(rt != null && rt.getClass() == org.omg.IOP.CodecPackage.FormatMismatch.class, "write/read round-trip class preserved");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.CodecPackage.FormatMismatchHelper.insert(a, new org.omg.IOP.CodecPackage.FormatMismatch("n"));
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.extract(a) != null, "insert/extract Any round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type after insert is tk_except");
        // two back-to-back in one stream
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        org.omg.IOP.CodecPackage.FormatMismatchHelper.write(os2, new org.omg.IOP.CodecPackage.FormatMismatch());
        org.omg.IOP.CodecPackage.FormatMismatchHelper.write(os2, new org.omg.IOP.CodecPackage.FormatMismatch());
        org.omg.CORBA.portable.InputStream is2 = os2.create_input_stream();
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.read(is2) != null && org.omg.IOP.CodecPackage.FormatMismatchHelper.read(is2) != null, "two exceptions back-to-back in one stream");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
