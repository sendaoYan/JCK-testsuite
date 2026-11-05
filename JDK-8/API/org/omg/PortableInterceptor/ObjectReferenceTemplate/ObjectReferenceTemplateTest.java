/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplate conformance: interface shape.
 * @run main ObjectReferenceTemplateTest
 */

public class ObjectReferenceTemplateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ObjectReferenceTemplate");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ObjectReferenceFactory.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ObjectReferenceFactory");
        // ---- deepened (jdk8u372-b07 probed): template interface extends factory ----
        check(org.omg.PortableInterceptor.ObjectReferenceFactory.class.isAssignableFrom(c),
              "ObjectReferenceTemplate IS-A ObjectReferenceFactory");
        java.util.Set<String> sup = new java.util.TreeSet<String>();
        for (Class<?> i : org.omg.PortableInterceptor.ObjectReferenceTemplate.class.getInterfaces()) sup.add(i.getName());
        check(org.omg.PortableInterceptor.ObjectReferenceTemplate.class.getInterfaces().length == 1, "extends exactly 1 interfaces");
        check(sup.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("org.omg.PortableInterceptor.ObjectReferenceFactory"))), "interface list exact");
        java.util.Set<String> ortNames = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ObjectReferenceTemplate.class.getDeclaredMethods())
            ortNames.add(m.getName());
        check(ortNames.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                  "orb_id", "server_id", "adapter_name"))), "template adds exactly orb_id/server_id/adapter_name");
        check(org.omg.PortableInterceptor.ObjectReferenceTemplate.class.getDeclaredMethods().length == 3, "3 methods");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
