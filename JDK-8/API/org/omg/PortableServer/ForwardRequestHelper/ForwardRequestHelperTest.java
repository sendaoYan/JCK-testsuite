/*
 * @test
 * @summary org.omg.PortableServer.ForwardRequestHelper conformance: helper static id/type/narrow.
 * @run main ForwardRequestHelperTest
 */

public class ForwardRequestHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.ForwardRequestHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.ForwardRequestHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): id exact / tk_except / forward_reference 过 Any 通道 ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.PortableServer.ForwardRequestHelper.id().equals(
                  "IDL:omg.org/PortableServer/ForwardRequest:1.0"), "id exact");
        check(org.omg.PortableServer.ForwardRequestHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "type().kind() == tk_except");
        check(org.omg.PortableServer.ForwardRequestHelper.type().id().equals(
                  "IDL:omg.org/PortableServer/ForwardRequest:1.0"), "type().id() == id()");
        check(org.omg.PortableServer.ForwardRequestHelper.type().member_count() == 1,
              "exception TypeCode member_count == 1");
        check(org.omg.PortableServer.ForwardRequestHelper.type().member_name(0).equals("forward_reference"),
              "member_name(0) == forward_reference");
        check(org.omg.PortableServer.ForwardRequestHelper.type().member_type(0).kind() == org.omg.CORBA.TCKind.tk_objref,
              "member forward_reference maps to tk_objref");
        final org.omg.CORBA.Object fwd = new org.omg.CORBA.portable.ObjectImpl() {
            public String[] _ids() { return new String[]{"IDL:fake:1.0"}; }
        };
        org.omg.CORBA.Any hAny = orb.create_any();
        org.omg.PortableServer.ForwardRequestHelper.insert(hAny, new org.omg.PortableServer.ForwardRequest(null, fwd));
        check(hAny.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any kind tk_except after insert");
        check(org.omg.PortableServer.ForwardRequestHelper.extract(hAny).getClass()
                  == org.omg.PortableServer.ForwardRequest.class, "Any round-trip preserves class");
        check(org.omg.PortableServer.ForwardRequestHelper.extract(hAny).forward_reference == fwd,
              "Any round-trip keeps forward_reference object identity");
        org.omg.CORBA.portable.OutputStream hos = orb.create_output_stream();
        org.omg.PortableServer.ForwardRequestHelper.write(hos, new org.omg.PortableServer.ForwardRequest(null, null));
        check(org.omg.PortableServer.ForwardRequestHelper.read(hos.create_input_stream()).forward_reference == null,
              "CDR round-trip keeps null forward");
        try {
            org.omg.PortableServer.ForwardRequestHelper.extract(orb.create_any());
            check(false, "extract of empty Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "extract(empty Any) -> org.omg.CORBA.MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
