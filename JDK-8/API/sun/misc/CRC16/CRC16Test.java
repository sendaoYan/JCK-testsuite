/*
 * @test
 * @summary sun.misc.CRC16 conformance: class and members.
 * @run main CRC16Test
 */

public class CRC16Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.CRC16", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.CRC16"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("reset"), "declares method reset");
        check(mnames.contains("update"), "declares method update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
