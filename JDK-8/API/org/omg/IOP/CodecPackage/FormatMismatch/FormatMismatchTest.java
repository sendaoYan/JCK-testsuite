/*
 * @test
 * @summary org.omg.IOP.CodecPackage.FormatMismatch conformance: UserException hierarchy and constructors.
 * @run main FormatMismatchTest
 */

public class FormatMismatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.IOP.CodecPackage.FormatMismatch.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.IOP.CodecPackage.FormatMismatch.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.CodecPackage.FormatMismatch.class), "implements IDLEntity");
        org.omg.IOP.CodecPackage.FormatMismatch e = new org.omg.IOP.CodecPackage.FormatMismatch();
        org.omg.IOP.CodecPackage.FormatMismatch em = new org.omg.IOP.CodecPackage.FormatMismatch("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor chain / toString / helper wire ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.CodecPackage.FormatMismatch.class.getModifiers()), "class is final");
        check(org.omg.IOP.CodecPackage.FormatMismatch.class.getDeclaredConstructors().length == 2, "exactly two constructors () and (String)");
        org.omg.IOP.CodecPackage.FormatMismatch e0 = new org.omg.IOP.CodecPackage.FormatMismatch();
        check(e0.toString().equals("org.omg.IOP.CodecPackage.FormatMismatch: IDL:omg.org/IOP/Codec/FormatMismatch:1.0"), "no-arg toString is exactly <fqcn>: <repository id>");
        check(e0.getMessage().equals("IDL:omg.org/IOP/Codec/FormatMismatch:1.0"), "no-arg getMessage is exactly the repository id");
        org.omg.IOP.CodecPackage.FormatMismatch e1 = new org.omg.IOP.CodecPackage.FormatMismatch("boom");
        check(e1.toString().equals("org.omg.IOP.CodecPackage.FormatMismatch: IDL:omg.org/IOP/Codec/FormatMismatch:1.0  boom"), "message ctor toString: repository id + two spaces + message");
        check(e1.getMessage().equals("IDL:omg.org/IOP/Codec/FormatMismatch:1.0  boom"), "getMessage = repository id + two spaces + user message");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.CodecPackage.FormatMismatchHelper.write(os, e1);
        org.omg.IOP.CodecPackage.FormatMismatch rt = org.omg.IOP.CodecPackage.FormatMismatchHelper.read(os.create_input_stream());
        check(rt.getClass() == org.omg.IOP.CodecPackage.FormatMismatch.class, "helper wire round-trip preserves class");
        check(!rt.toString().endsWith("boom"), "helper wire round-trip drops the message (IDL exceptions carry no reason on CDR)");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.CodecPackage.FormatMismatchHelper.insert(a, e1);
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.extract(a).getClass() == org.omg.IOP.CodecPackage.FormatMismatch.class, "Any insert/extract round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type after insert is tk_except");
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.id().equals("IDL:omg.org/IOP/Codec/FormatMismatch:1.0"), "helper id() exact");
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "helper type().kind() == tk_except");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
