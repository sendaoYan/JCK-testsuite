/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateHolder conformance: holder value field.
 * @run main ObjectReferenceTemplateHolderTest
 */

public class ObjectReferenceTemplateHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.ObjectReferenceTemplateHolder h0 = new org.omg.PortableInterceptor.ObjectReferenceTemplateHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.ObjectReferenceTemplate v = null;
        org.omg.PortableInterceptor.ObjectReferenceTemplateHolder h1 = new org.omg.PortableInterceptor.ObjectReferenceTemplateHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): valuetype holder ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.ObjectReferenceTemplateHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.PortableInterceptor.ObjectReferenceTemplateHolder.class),
              "implements Streamable");
        check(new org.omg.PortableInterceptor.ObjectReferenceTemplateHolder().value == null, "no-arg default null");
        check(new org.omg.PortableInterceptor.ObjectReferenceTemplateHolder()._type().kind() == org.omg.CORBA.TCKind.tk_value,
              "_type().kind() == tk_value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
