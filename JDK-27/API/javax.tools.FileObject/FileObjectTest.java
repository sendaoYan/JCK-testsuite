/*
 * @test
 * @summary javax.tools.FileObject conformance: interface.
 * @run main FileObjectTest
 */

import java.lang.reflect.*;


public class FileObjectTest {
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
        Class<?> c = Class.forName("javax.tools.FileObject");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "toUri"), "declares toUri");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "openInputStream"), "declares openInputStream");
        check(hasMethod(c, "openOutputStream"), "declares openOutputStream");
        check(hasMethod(c, "openReader"), "declares openReader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
