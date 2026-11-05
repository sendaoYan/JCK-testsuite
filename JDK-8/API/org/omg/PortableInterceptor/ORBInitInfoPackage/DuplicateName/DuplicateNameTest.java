/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName conformance: UserException hierarchy and constructors.
 * @run main DuplicateNameTest
 */

public class DuplicateNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class), "implements IDLEntity");
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e = new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName();
        check(e.name == null, "name default null");
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName em = new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName("msg", null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): 3-ctor chain with name member ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class.getModifiers()), "final");
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName d0 =
            new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName();
        check(d0.name == null, "no-arg name field null");
        check(d0.toString().equals(
                  "org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName: IDL:omg.org/PortableInterceptor/ORBInitInfo/DuplicateName:1.0"),
              "no-arg toString exact");
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName d1 =
            new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName("OnlyInterceptor");
        check(d1.name.equals("OnlyInterceptor"), "(String) ctor sets the name member");
        check(d1.getMessage().equals("IDL:omg.org/PortableInterceptor/ORBInitInfo/DuplicateName:1.0"),
              "(String) ctor leaves the reason empty: message stays the bare repository id");
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName d2 =
            new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName("dup!", "MyInterceptor");
        check(d2.name.equals("MyInterceptor"), "(reason, name) ctor sets name member exactly");
        check(d2.getMessage().equals(
                  "IDL:omg.org/PortableInterceptor/ORBInitInfo/DuplicateName:1.0  dup!"),
              "(reason,name) ctor message = repo id + two spaces + reason");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.write(os, d2);
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName rt =
            org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.read(os.create_input_stream());
        check(rt.getClass() == org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class,
              "wire round-trip preserves class");
        check(rt.name.equals("MyInterceptor"), "wire round-trip preserves name member");
        check(!rt.toString().endsWith("dup!"), "wire round-trip drops reason");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.insert(a, d2);
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.extract(a).name.equals("MyInterceptor"),
              "Any insert/extract round-trip preserves name");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
