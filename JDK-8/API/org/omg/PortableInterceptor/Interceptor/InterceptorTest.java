/*
 * @test
 * @summary org.omg.PortableInterceptor.Interceptor conformance: interface shape.
 * @run main InterceptorTest
 */

public class InterceptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.Interceptor");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.InterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.InterceptorOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): lifecycle interface ----
        check(c.getDeclaredMethods().length == 0, "conformance interface adds no methods");
        java.util.Set<String> sup = new java.util.TreeSet<String>();
        for (Class<?> i : org.omg.PortableInterceptor.Interceptor.class.getInterfaces()) sup.add(i.getName());
        check(org.omg.PortableInterceptor.Interceptor.class.getInterfaces().length == 3, "extends exactly 3 interfaces");
        check(sup.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("org.omg.PortableInterceptor.InterceptorOperations", "org.omg.CORBA.Object", "org.omg.CORBA.portable.IDLEntity"))), "interface list exact");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
