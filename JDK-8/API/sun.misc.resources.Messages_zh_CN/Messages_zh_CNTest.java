/*
 * @test
 * @summary sun.misc.resources.Messages_zh_CN conformance: class and members.
 * @run main Messages_zh_CNTest
 */

public class Messages_zh_CNTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.resources.Messages_zh_CN", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.resources.Messages_zh_CN"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass java.util.ListResourceBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getContents"), "declares method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
