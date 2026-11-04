/*
 * @test
 * @summary sun.management.StackTraceElementCompositeData conformance: class and members.
 * @run main StackTraceElementCompositeDataTest
 */

public class StackTraceElementCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.StackTraceElementCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.StackTraceElementCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("from"), "declares method from");
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getStackTraceElement"), "declares method getStackTraceElement");
        check(mnames.contains("toCompositeData"), "declares method toCompositeData");
        check(mnames.contains("validateCompositeData"), "declares method validateCompositeData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
