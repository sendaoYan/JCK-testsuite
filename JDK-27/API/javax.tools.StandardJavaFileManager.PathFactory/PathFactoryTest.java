/*
 * @test
 * @summary javax.tools.StandardJavaFileManager.PathFactory conformance: white-box structure + black-box behavior.
 * @run main PathFactoryTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;

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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.StandardJavaFileManager$PathFactory");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getPath"), "declares getPath");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A PathFactory joins path strings into a Path.
        javax.tools.StandardJavaFileManager.PathFactory pf =
                new javax.tools.StandardJavaFileManager.PathFactory() {
            public Path getPath(String first, String... more) {
                return Path.of(first, more);
            }
        };
        check(pf.getPath("a").equals(Path.of("a")), "getPath single segment");
        check(pf.getPath("a", "b", "c").equals(Path.of("a", "b", "c")), "getPath multiple segments");

        // The factory can be installed on a real StandardJavaFileManager.
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc != null) {
            javax.tools.StandardJavaFileManager fm =
                    jc.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
            try {
                final boolean[] called = new boolean[1];
                fm.setPathFactory(new javax.tools.StandardJavaFileManager.PathFactory() {
                    public Path getPath(String first, String... more) {
                        called[0] = true;
                        return Path.of(first, more);
                    }
                });
                check(true, "setPathFactory accepts a factory");
                try {
                    fm.setPathFactory(null);
                    throw new AssertionError("setPathFactory(null) should throw NPE");
                } catch (NullPointerException expected) {
                    check(true, "setPathFactory(null) throws NPE");
                }
            } finally {
                fm.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
