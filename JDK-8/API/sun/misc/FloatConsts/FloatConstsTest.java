/*
 * @test
 * @summary sun.misc.FloatConsts conformance: class and members.
 * @run main FloatConstsTest
 */

public class FloatConstsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.FloatConsts", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.FloatConsts"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
