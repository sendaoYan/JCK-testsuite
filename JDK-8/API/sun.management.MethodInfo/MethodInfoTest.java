/*
 * @test
 * @summary sun.management.MethodInfo conformance: class and members.
 * @run main MethodInfoTest
 */

public class MethodInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.MethodInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.MethodInfo"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Serializable"), "interface java.io.Serializable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCompileSize"), "declares method getCompileSize");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getType"), "declares method getType");
        check(mnames.contains("toString"), "declares method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
