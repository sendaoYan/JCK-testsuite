/*
 * @test
 * @summary sun.reflect.Reflection conformance: class and members.
 * @run main ReflectionTest
 */

public class ReflectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.Reflection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.Reflection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("ensureMemberAccess"), "declares method ensureMemberAccess");
        check(mnames.contains("filter"), "declares method filter");
        check(mnames.contains("filterFields"), "declares method filterFields");
        check(mnames.contains("filterMethods"), "declares method filterMethods");
        check(mnames.contains("getCallerClass"), "declares method getCallerClass");
        check(mnames.contains("getClassAccessFlags"), "declares method getClassAccessFlags");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
