/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder conformance: holder value field.
 * @run main ObjectReferenceTemplateSeqHolderTest
 */

public class ObjectReferenceTemplateSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder h0 = new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.ObjectReferenceTemplate[] v = null;
        org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder h1 = new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): array holder ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder.class),
              "implements Streamable");
        check(new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder().value == null, "no-arg default null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder(
            new org.omg.PortableInterceptor.ObjectReferenceTemplate[0])._write(os);
        org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder hr =
            new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder();
        hr._read(os.create_input_stream());
        check(hr.value != null && hr.value.length == 0, "empty-array _write/_read round-trip");
        check(new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "_type().kind() == tk_alias");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
