/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceFactoryHelper conformance: helper static id/type/narrow.
 * @run main ObjectReferenceFactoryHelperTest
 */

public class ObjectReferenceFactoryHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): valuetype helper ----
        check(org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ObjectReferenceFactory:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.type().kind() == org.omg.CORBA.TCKind.tk_value,
              "type().kind() == tk_value (abstract valuetype)");
        // no narrow/unchecked_narrow on valuetype helpers
        boolean hasNarrow = false;
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.class.getDeclaredMethods())
            if (m.getName().contains("narrow")) hasNarrow = true;
        check(!hasNarrow, "valuetype helper has no narrow methods");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        boolean marshal = false;
        try { org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.extract(orb.create_any()); }
        catch (org.omg.CORBA.MARSHAL m) { marshal = true; }
        check(marshal, "extract(empty any) -> MARSHAL");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
