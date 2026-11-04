/*
 * @test
 * @summary sun.misc.RegexpPool conformance: class and members.
 * @run main RegexpPoolTest
 */

public class RegexpPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.RegexpPool", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.RegexpPool"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("delete"), "declares method delete");
        check(mnames.contains("match"), "declares method match");
        check(mnames.contains("matchAfter"), "declares method matchAfter");
        check(mnames.contains("matchNext"), "declares method matchNext");
        check(mnames.contains("print"), "declares method print");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
