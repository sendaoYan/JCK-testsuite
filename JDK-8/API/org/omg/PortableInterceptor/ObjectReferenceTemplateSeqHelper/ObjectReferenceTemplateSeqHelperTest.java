/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper conformance: helper static id/type/narrow.
 * @run main ObjectReferenceTemplateSeqHelperTest
 */

public class ObjectReferenceTemplateSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): sequence-of-valuetype helper ----
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ObjectReferenceTemplateSeq:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // empty sequence marshals without touching any template value
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.write(os,
            new org.omg.PortableInterceptor.ObjectReferenceTemplate[0]);
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.read(os.create_input_stream()).length == 0,
              "empty sequence wire round-trip length 0");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.insert(a,
            new org.omg.PortableInterceptor.ObjectReferenceTemplate[0]);
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.extract(a).length == 0,
              "empty sequence Any round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
