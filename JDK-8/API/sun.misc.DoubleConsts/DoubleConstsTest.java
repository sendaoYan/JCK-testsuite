/*
 * @test
 * @summary sun.misc.DoubleConsts conformance: class and members.
 * @run main DoubleConstsTest
 */

public class DoubleConstsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.DoubleConsts", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.DoubleConsts"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
