/*
 * @test
 * @summary sun.net.NetProperties conformance: class and members.
 * @run main NetPropertiesTest
 */

public class NetPropertiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.NetProperties", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.NetProperties"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getBoolean"), "declares method getBoolean");
        check(mnames.contains("getInteger"), "declares method getInteger");
        check(mnames.contains("loadDefaultProperties"), "declares method loadDefaultProperties");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
