/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptor_3_0Holder conformance: holder value field.
 * @run main IORInterceptor_3_0HolderTest
 */

public class IORInterceptor_3_0HolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.IORInterceptor_3_0Holder h0 = new org.omg.PortableInterceptor.IORInterceptor_3_0Holder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.IORInterceptor_3_0 v = null;
        org.omg.PortableInterceptor.IORInterceptor_3_0Holder h1 = new org.omg.PortableInterceptor.IORInterceptor_3_0Holder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): objref holder ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.IORInterceptor_3_0Holder.class.getModifiers()), "final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.PortableInterceptor.IORInterceptor_3_0Holder.class),
              "implements Streamable");
        check(new org.omg.PortableInterceptor.IORInterceptor_3_0Holder().value == null, "no-arg default null");
        org.omg.PortableInterceptor.IORInterceptor_3_0Holder h =
            new org.omg.PortableInterceptor.IORInterceptor_3_0Holder(null);
        check(h.value == null, "ctor stores null");
        check(new org.omg.PortableInterceptor.IORInterceptor_3_0Holder()._type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "_type().kind() == tk_objref");
        check(new org.omg.PortableInterceptor.IORInterceptor_3_0Holder()._type().equal(
                  org.omg.PortableInterceptor.IORInterceptor_3_0Helper.type()),
              "_type() equals Helper.type()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
