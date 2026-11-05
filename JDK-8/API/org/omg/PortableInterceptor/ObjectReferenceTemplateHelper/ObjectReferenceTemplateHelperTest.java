/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateHelper conformance: helper static id/type/narrow.
 * @run main ObjectReferenceTemplateHelperTest
 */

public class ObjectReferenceTemplateHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): valuetype helper ----
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ObjectReferenceTemplate:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.type().kind() == org.omg.CORBA.TCKind.tk_value,
              "type().kind() == tk_value");
        boolean hasNarrow = false;
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.class.getDeclaredMethods())
            if (m.getName().contains("narrow")) hasNarrow = true;
        check(!hasNarrow, "valuetype helper has no narrow methods");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        boolean marshal = false;
        try { org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.extract(orb.create_any()); }
        catch (org.omg.CORBA.MARSHAL m) { marshal = true; }
        check(marshal, "extract(empty any) -> MARSHAL");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
