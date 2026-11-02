/*
 * @test
 * @summary javax.tools.StandardJavaFileManager.PathFactory conformance: interface.
 * @run main PathFactoryTest
 */

import java.lang.reflect.*;


public class PathFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.tools.StandardJavaFileManager$PathFactory");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getPath"), "declares getPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
