/*
 * @test
 * @summary org.omg.PortableInterceptor.ClientRequestInterceptor conformance: interface shape.
 * @run main ClientRequestInterceptorTest
 */

public class ClientRequestInterceptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ClientRequestInterceptor");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ClientRequestInterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ClientRequestInterceptorOperations");
        check(org.omg.PortableInterceptor.Interceptor.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.Interceptor");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): hook interface shape ----
        check(c.getDeclaredMethods().length == 0, "conformance interface adds no methods");
        java.util.Set<String> sup = new java.util.TreeSet<String>();
        for (Class<?> i : org.omg.PortableInterceptor.ClientRequestInterceptor.class.getInterfaces()) sup.add(i.getName());
        check(org.omg.PortableInterceptor.ClientRequestInterceptor.class.getInterfaces().length == 3, "extends exactly 3 interfaces");
        check(sup.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("org.omg.PortableInterceptor.ClientRequestInterceptorOperations", "org.omg.PortableInterceptor.Interceptor", "org.omg.CORBA.portable.IDLEntity"))), "interface list exact");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "indirectly extends CORBA.Object via Interceptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
