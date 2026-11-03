/*
 * @test
 * @summary javax.tools.OptionChecker conformance: white-box structure + black-box behavior.
 * @run main OptionCheckerTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class OptionCheckerTest {
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
        Class<?> c = Class.forName("javax.tools.OptionChecker");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "isSupportedOption"), "declares isSupportedOption");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // JavaCompiler is an OptionChecker: recognized options report their arg count.
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            return;
        }
        javax.tools.OptionChecker oc = jc;
        check(oc.isSupportedOption("-source") == 1, "-source consumes 1 arg");
        check(oc.isSupportedOption("-target") == 1, "-target consumes 1 arg");
        check(oc.isSupportedOption("-g") == 0, "-g consumes 0 args");
        check(oc.isSupportedOption("-Xlint") == 0, "-Xlint consumes 0 args");
        check(oc.isSupportedOption("--bogus") == -1, "unknown option -1");

        // StandardJavaFileManager is an OptionChecker for path options.
        javax.tools.StandardJavaFileManager fm =
                jc.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        try {
            javax.tools.OptionChecker fmChecker = fm;
            check(fmChecker.isSupportedOption("-classpath") == 1, "fm -classpath == 1");
            check(fmChecker.isSupportedOption("-cp") == 1, "fm -cp == 1");
            check(fmChecker.isSupportedOption("-sourcepath") == 1, "fm -sourcepath == 1");
            check(fmChecker.isSupportedOption("--bogus") == -1, "fm unknown option -1");
        } finally {
            fm.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
