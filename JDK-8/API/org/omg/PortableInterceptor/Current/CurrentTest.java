/*
 * @test
 * @summary org.omg.PortableInterceptor.Current conformance: interface shape.
 * @run main CurrentTest
 */

public class CurrentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.Current");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.CurrentOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.CurrentOperations");
        check(org.omg.CORBA.Current.class.isAssignableFrom(c), "extends org.omg.CORBA.Current");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): PICurrent shape ----
        check(c.getDeclaredMethods().length == 0, "conformance interface adds no methods");
        java.util.Set<String> sup = new java.util.TreeSet<String>();
        for (Class<?> i : org.omg.PortableInterceptor.Current.class.getInterfaces()) sup.add(i.getName());
        check(org.omg.PortableInterceptor.Current.class.getInterfaces().length == 3, "extends exactly 3 interfaces");
        check(sup.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("org.omg.PortableInterceptor.CurrentOperations", "org.omg.CORBA.Current", "org.omg.CORBA.portable.IDLEntity"))), "interface list exact");
        check(org.omg.CORBA.CurrentOperations.class.isAssignableFrom(c),
              "indirectly extends org.omg.CORBA.CurrentOperations via CORBA.Current");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c),
              "indirectly extends CORBA.Object via CORBA.Current");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
