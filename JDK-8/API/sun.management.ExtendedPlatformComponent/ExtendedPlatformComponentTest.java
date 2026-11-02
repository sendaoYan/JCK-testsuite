/*
 * @test
 * @summary sun.management.ExtendedPlatformComponent conformance: class and members.
 * @run main ExtendedPlatformComponentTest
 */

public class ExtendedPlatformComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.ExtendedPlatformComponent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.ExtendedPlatformComponent"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getFlightRecorderBean"), "declares method getFlightRecorderBean");
        check(mnames.contains("getMXBean"), "declares method getMXBean");
        check(mnames.contains("getMXBeans"), "declares method getMXBeans");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
