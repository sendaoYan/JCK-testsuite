/*
 * @test
 * @summary javax.tools.JavaFileManager conformance: reflection shape checks.
 * @run main JavaFileManagerTest
 */

import java.lang.reflect.*;

public class JavaFileManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.tools.JavaFileManager");
        check(c != null, "javax.tools.JavaFileManager loads");
        check(c.getName().equals("javax.tools.JavaFileManager"), "binary name is javax.tools.JavaFileManager");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.JavaFileManager is public");
        check(c.isInterface(), "javax.tools.JavaFileManager is an interface");
        check(ifaceNames(c).equals(set("java.io.Closeable", "java.io.Flushable", "javax.tools.OptionChecker")), "direct superinterfaces");
        check(declares(c, "getClassLoader"), "declares getClassLoader");
        check(declares(c, "list"), "declares list");
        check(declares(c, "inferBinaryName"), "declares inferBinaryName");
        check(declares(c, "isSameFile"), "declares isSameFile");
        check(declares(c, "handleOption"), "declares handleOption");
        check(declares(c, "hasLocation"), "declares hasLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
