/*
 * @test
 * @summary sun.util.locale.StringTokenIterator conformance: class and members.
 * @run main StringTokenIteratorTest
 */

public class StringTokenIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.StringTokenIterator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.StringTokenIterator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("current"), "declares method current");
        check(mnames.contains("currentEnd"), "declares method currentEnd");
        check(mnames.contains("currentStart"), "declares method currentStart");
        check(mnames.contains("first"), "declares method first");
        check(mnames.contains("hasNext"), "declares method hasNext");
        check(mnames.contains("isDone"), "declares method isDone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
