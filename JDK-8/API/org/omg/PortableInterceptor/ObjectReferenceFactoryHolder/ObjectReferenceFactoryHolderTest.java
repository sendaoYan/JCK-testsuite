/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceFactoryHolder conformance: holder value field.
 * @run main ObjectReferenceFactoryHolderTest
 */

public class ObjectReferenceFactoryHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.ObjectReferenceFactoryHolder h0 = new org.omg.PortableInterceptor.ObjectReferenceFactoryHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.ObjectReferenceFactory v = null;
        org.omg.PortableInterceptor.ObjectReferenceFactoryHolder h1 = new org.omg.PortableInterceptor.ObjectReferenceFactoryHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): valuetype holder ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.ObjectReferenceFactoryHolder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.PortableInterceptor.ObjectReferenceFactoryHolder.class),
              "implements Streamable");
        check(new org.omg.PortableInterceptor.ObjectReferenceFactoryHolder().value == null, "no-arg default null");
        org.omg.PortableInterceptor.ObjectReferenceFactoryHolder h =
            new org.omg.PortableInterceptor.ObjectReferenceFactoryHolder(null);
        check(h.value == null, "ctor stores null");
        check(new org.omg.PortableInterceptor.ObjectReferenceFactoryHolder()._type().kind() == org.omg.CORBA.TCKind.tk_value,
              "_type().kind() == tk_value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
