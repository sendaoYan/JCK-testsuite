/*
 * @test
 * @summary org.omg.PortableInterceptor.ForwardRequest conformance: UserException hierarchy and constructors.
 * @run main ForwardRequestTest
 */

public class ForwardRequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.ForwardRequest.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.ForwardRequest.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.ForwardRequest.class), "implements IDLEntity");
        org.omg.PortableInterceptor.ForwardRequest e = new org.omg.PortableInterceptor.ForwardRequest();
        check(e.forward == null, "forward default null");
        org.omg.PortableInterceptor.ForwardRequest em = new org.omg.PortableInterceptor.ForwardRequest("msg", null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor matrix / field / toString / wire ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.ForwardRequest.class.getModifiers()), "final");
        check(org.omg.PortableInterceptor.ForwardRequest.class.getDeclaredConstructors().length == 3,
              "exactly 3 ctors: (), (Object forward), (String reason, Object forward)");
        check(new org.omg.PortableInterceptor.ForwardRequest().toString().equals(
                  "org.omg.PortableInterceptor.ForwardRequest: IDL:omg.org/PortableInterceptor/ForwardRequest:1.0"),
              "no-arg toString exact");
        check(new org.omg.PortableInterceptor.ForwardRequest("fwd", null).toString().equals(
                  "org.omg.PortableInterceptor.ForwardRequest: IDL:omg.org/PortableInterceptor/ForwardRequest:1.0  fwd"),
              "message ctor toString = repo id + two spaces + message");
        check(new org.omg.PortableInterceptor.ForwardRequest((org.omg.CORBA.Object) null).forward == null,
              "(Object) ctor stores null forward");
        check(org.omg.PortableInterceptor.ForwardRequest.class.getField("forward").getType() == org.omg.CORBA.Object.class,
              "forward field type is org.omg.CORBA.Object (public)");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ForwardRequestHelper.write(os, new org.omg.PortableInterceptor.ForwardRequest("m", null));
        org.omg.PortableInterceptor.ForwardRequest rt = org.omg.PortableInterceptor.ForwardRequestHelper.read(os.create_input_stream());
        check(rt.getClass() == org.omg.PortableInterceptor.ForwardRequest.class, "helper wire round-trip preserves class");
        check(rt.forward == null, "wire round-trip forward null preserved");
        check(!rt.toString().endsWith("m"), "wire round-trip drops the reason message");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ForwardRequestHelper.insert(a, new org.omg.PortableInterceptor.ForwardRequest(null));
        check(org.omg.PortableInterceptor.ForwardRequestHelper.extract(a) != null, "Any insert/extract round-trip");
        // writing a local object reference as forward fails: write_Object rejects local objects
        final org.omg.IOP.CodecFactory localCf = org.omg.IOP.CodecFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("CodecFactory"));
        boolean marshalLocal = false;
        try {
            org.omg.CORBA.portable.OutputStream osL = orb.create_output_stream();
            org.omg.PortableInterceptor.ForwardRequestHelper.write(osL,
                new org.omg.PortableInterceptor.ForwardRequest("m", localCf));
        } catch (org.omg.CORBA.MARSHAL m) { marshalLocal = true; }
        check(marshalLocal, "wire write with local-object forward -> org.omg.CORBA.MARSHAL");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
