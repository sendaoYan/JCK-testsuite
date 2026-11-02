/*
 * @test
 * @summary javax.tools.StandardJavaFileManager conformance: interface.
 * @run main StandardJavaFileManagerTest
 */

import java.lang.reflect.*;


public class StandardJavaFileManagerTest {
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
        Class<?> c = Class.forName("javax.tools.StandardJavaFileManager");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.tools.JavaFileManager"), "extends javax.tools.JavaFileManager");
        check(hasMethod(c, "isSameFile"), "declares isSameFile");
        check(hasMethod(c, "getJavaFileObjectsFromFiles"), "declares getJavaFileObjectsFromFiles");
        check(hasMethod(c, "getJavaFileObjectsFromPaths"), "declares getJavaFileObjectsFromPaths");
        check(hasMethod(c, "getJavaFileObjects"), "declares getJavaFileObjects");
        check(hasMethod(c, "getJavaFileObjectsFromStrings"), "declares getJavaFileObjectsFromStrings");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
