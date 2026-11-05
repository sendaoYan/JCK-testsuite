/*
 * @test
 * @summary org.omg.PortableInterceptor.ClientRequestInfo conformance: interface shape.
 * @run main ClientRequestInfoTest
 */

public class ClientRequestInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ClientRequestInfo");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ClientRequestInfoOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ClientRequestInfoOperations");
        check(org.omg.PortableInterceptor.RequestInfo.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.RequestInfo");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(c.getDeclaredMethods().length == 0, "conformance interface adds no methods");
        java.util.Set<String> sup = new java.util.TreeSet<String>();
        for (Class<?> i : org.omg.PortableInterceptor.ClientRequestInfo.class.getInterfaces()) sup.add(i.getName());
        check(org.omg.PortableInterceptor.ClientRequestInfo.class.getInterfaces().length == 3, "extends exactly 3 interfaces");
        check(sup.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("org.omg.PortableInterceptor.ClientRequestInfoOperations", "org.omg.PortableInterceptor.RequestInfo", "org.omg.CORBA.portable.IDLEntity"))), "interface list exact");
        check(org.omg.PortableInterceptor.RequestInfoOperations.class.isAssignableFrom(c),
              "indirectly extends RequestInfoOperations via RequestInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
