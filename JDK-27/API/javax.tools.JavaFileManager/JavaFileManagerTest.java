/*
 * @test
 * @summary javax.tools.JavaFileManager conformance: interface.
 * @run main JavaFileManagerTest
 */

import java.lang.reflect.*;


public class JavaFileManagerTest {
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
        Class<?> c = Class.forName("javax.tools.JavaFileManager");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 3, "3 superinterface(s)");
        check(hasInterface(c, "java.io.Closeable"), "extends java.io.Closeable");
        check(hasInterface(c, "java.io.Flushable"), "extends java.io.Flushable");
        check(hasInterface(c, "javax.tools.OptionChecker"), "extends javax.tools.OptionChecker");
        check(hasMethod(c, "getClassLoader"), "declares getClassLoader");
        check(hasMethod(c, "list"), "declares list");
        check(hasMethod(c, "inferBinaryName"), "declares inferBinaryName");
        check(hasMethod(c, "isSameFile"), "declares isSameFile");
        check(hasMethod(c, "handleOption"), "declares handleOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
