/*
 * @test
 * @summary sun.management.MappedMXBeanType conformance: abstract class and members.
 * @run main MappedMXBeanTypeTest
 */

public class MappedMXBeanTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.MappedMXBeanType", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.MappedMXBeanType"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("decapitalize"), "declares method decapitalize");
        check(mnames.contains("getJavaType"), "declares method getJavaType");
        check(mnames.contains("getMappedType"), "declares method getMappedType");
        check(mnames.contains("getMappedTypeClass"), "declares method getMappedTypeClass");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getOpenType"), "declares method getOpenType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
