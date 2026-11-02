/*
 * @test
 * @summary sun.nio.cs.ext.ISO2022_JP_2 conformance: class and members.
 * @run main ISO2022_JP_2Test
 */

public class ISO2022_JP_2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.ISO2022_JP_2", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.ISO2022_JP_2"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.ext.ISO2022_JP"), "superclass sun.nio.cs.ext.ISO2022_JP");
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
