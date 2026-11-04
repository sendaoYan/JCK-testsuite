/*
 * @test
 * @summary sun.net.www.content.text.Generic conformance: class and members.
 * @run main GenericTest
 */

public class GenericTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.content.text.Generic", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.content.text.Generic"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.content.text.plain"), "superclass sun.net.www.content.text.plain");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
