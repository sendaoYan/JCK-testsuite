/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName conformance: UserException hierarchy and constructors.
 * @run main InvalidNameTest
 */

public class InvalidNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class), "implements IDLEntity");
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName e = new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName();
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName em = new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor chain / toString / helper wire ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class.getModifiers()), "final");
        check(new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName().toString().equals(
                  "org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName: IDL:omg.org/PortableInterceptor/ORBInitInfo/InvalidName:1.0"),
              "no-arg toString exact");
        check(new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName("nm").toString().equals(
                  "org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName: IDL:omg.org/PortableInterceptor/ORBInitInfo/InvalidName:1.0  nm"),
              "message ctor toString exact");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.write(os,
            new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName("bad"));
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName rt =
            org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.read(os.create_input_stream());
        check(rt.getClass() == org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class, "wire round-trip class");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.insert(a,
            new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName("n2"));
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.extract(a) != null, "Any round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type tk_except");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
