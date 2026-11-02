/*
 * @test
 * @summary sun.nio.cs.UTF_32BE conformance: class and members.
 * @run main UTF_32BETest
 */

public class UTF_32BETest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.UTF_32BE", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.UTF_32BE"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.Unicode"), "superclass sun.nio.cs.Unicode");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("contains"), "declares method contains");
        check(mnames.contains("historicalName"), "declares method historicalName");
        check(mnames.contains("newDecoder"), "declares method newDecoder");
        check(mnames.contains("newEncoder"), "declares method newEncoder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
