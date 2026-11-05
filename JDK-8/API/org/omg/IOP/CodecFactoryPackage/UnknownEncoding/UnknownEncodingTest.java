/*
 * @test
 * @summary org.omg.IOP.CodecFactoryPackage.UnknownEncoding conformance: UserException hierarchy and constructors.
 * @run main UnknownEncodingTest
 */

public class UnknownEncodingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class), "implements IDLEntity");
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding e = new org.omg.IOP.CodecFactoryPackage.UnknownEncoding();
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding em = new org.omg.IOP.CodecFactoryPackage.UnknownEncoding("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor chain / toString / helper wire ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class.getModifiers()), "class is final");
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class.getDeclaredConstructors().length == 2, "exactly two constructors () and (String)");
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding e0 = new org.omg.IOP.CodecFactoryPackage.UnknownEncoding();
        check(e0.toString().equals("org.omg.IOP.CodecFactoryPackage.UnknownEncoding: IDL:omg.org/IOP/CodecFactory/UnknownEncoding:1.0"), "no-arg toString is exactly <fqcn>: <repository id>");
        check(e0.getMessage().equals("IDL:omg.org/IOP/CodecFactory/UnknownEncoding:1.0"), "no-arg getMessage is exactly the repository id");
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding e1 = new org.omg.IOP.CodecFactoryPackage.UnknownEncoding("boom");
        check(e1.toString().equals("org.omg.IOP.CodecFactoryPackage.UnknownEncoding: IDL:omg.org/IOP/CodecFactory/UnknownEncoding:1.0  boom"), "message ctor toString: repository id + two spaces + message");
        check(e1.getMessage().equals("IDL:omg.org/IOP/CodecFactory/UnknownEncoding:1.0  boom"), "getMessage = repository id + two spaces + user message");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.write(os, e1);
        org.omg.IOP.CodecFactoryPackage.UnknownEncoding rt = org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.read(os.create_input_stream());
        check(rt.getClass() == org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class, "helper wire round-trip preserves class");
        check(!rt.toString().endsWith("boom"), "helper wire round-trip drops the message (IDL exceptions carry no reason on CDR)");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.insert(a, e1);
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.extract(a).getClass() == org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class, "Any insert/extract round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type after insert is tk_except");
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.id().equals("IDL:omg.org/IOP/CodecFactory/UnknownEncoding:1.0"), "helper id() exact");
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "helper type().kind() == tk_except");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
