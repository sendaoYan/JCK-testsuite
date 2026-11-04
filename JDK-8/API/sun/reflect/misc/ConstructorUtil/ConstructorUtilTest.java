/*
 * @test
 * @summary sun.reflect.misc.ConstructorUtil conformance: class and members.
 * @run main ConstructorUtilTest
 */

public class ConstructorUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.misc.ConstructorUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.misc.ConstructorUtil"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getConstructor"), "declares method getConstructor");
        check(mnames.contains("getConstructors"), "declares method getConstructors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
