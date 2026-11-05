/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceFactory conformance: interface shape.
 * @run main ObjectReferenceFactoryTest
 */

public class ObjectReferenceFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ObjectReferenceFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");
        // ---- deepened (jdk8u372-b07 probed): valuetype factory interface ----
        check(c.getDeclaredMethods().length == 1, "single method");
        java.util.Set<String> sup = new java.util.TreeSet<String>();
        for (Class<?> i : org.omg.PortableInterceptor.ObjectReferenceFactory.class.getInterfaces()) sup.add(i.getName());
        check(org.omg.PortableInterceptor.ObjectReferenceFactory.class.getInterfaces().length == 1, "extends exactly 1 interfaces");
        check(sup.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("org.omg.CORBA.portable.ValueBase"))), "interface list exact");
        java.lang.reflect.Method mo = org.omg.PortableInterceptor.ObjectReferenceFactory.class.getDeclaredMethod(
            "make_object", String.class, byte[].class);
        check(mo.getReturnType() == org.omg.CORBA.Object.class, "make_object(String, byte[]) returns CORBA Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "ValueBase-extending interface is Serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
