/*
 * @test
 * @summary sun.misc.ObjectInputFilter.Config conformance: class and members.
 * @run main ConfigTest
 */

public class ConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ObjectInputFilter$Config", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ObjectInputFilter$Config"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createFilter"), "declares method createFilter");
        check(mnames.contains("createFilter2"), "declares method createFilter2");
        check(mnames.contains("filterLog"), "declares method filterLog");
        check(mnames.contains("getObjectInputFilter"), "declares method getObjectInputFilter");
        check(mnames.contains("getSerialFilter"), "declares method getSerialFilter");
        check(mnames.contains("setObjectInputFilter"), "declares method setObjectInputFilter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
