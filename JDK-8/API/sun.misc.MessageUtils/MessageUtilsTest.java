/*
 * @test
 * @summary sun.misc.MessageUtils conformance: class and members.
 * @run main MessageUtilsTest
 */

public class MessageUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.MessageUtils", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.MessageUtils"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("err"), "declares method err");
        check(mnames.contains("out"), "declares method out");
        check(mnames.contains("subst"), "declares method subst");
        check(mnames.contains("substProp"), "declares method substProp");
        check(mnames.contains("toStderr"), "declares method toStderr");
        check(mnames.contains("toStdout"), "declares method toStdout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
