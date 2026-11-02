/*
 * @test
 * @summary javax.tools.JavaFileObject conformance: interface.
 * @run main JavaFileObjectTest
 */

import java.lang.reflect.*;


public class JavaFileObjectTest {
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
        Class<?> c = Class.forName("javax.tools.JavaFileObject");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.tools.FileObject"), "extends javax.tools.FileObject");
        check(hasMethod(c, "getKind"), "declares getKind");
        check(hasMethod(c, "isNameCompatible"), "declares isNameCompatible");
        check(hasMethod(c, "getNestingKind"), "declares getNestingKind");
        check(hasMethod(c, "getAccessLevel"), "declares getAccessLevel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
