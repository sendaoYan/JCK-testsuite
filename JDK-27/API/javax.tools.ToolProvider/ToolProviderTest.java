/*
 * @test
 * @summary javax.tools.ToolProvider conformance: white-box structure + black-box behavior.
 * @run main ToolProviderTest
 */

import java.lang.reflect.*;

public class ToolProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.ToolProvider");
        check(!c.isInterface(), "not an interface");
        check(hasMethod(c, "getSystemJavaCompiler"), "declares getSystemJavaCompiler");
        check(hasMethod(c, "getSystemDocumentationTool"), "declares getSystemDocumentationTool");
        check(hasMethod(c, "getSystemToolClassLoader"), "declares getSystemToolClassLoader");
        boolean allStatic = true;
        for (Method m : c.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) allStatic = false;
        }
        check(allStatic, "all methods static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        check(jc != null, "getSystemJavaCompiler non-null (full JDK)");
        if (jc != null) {
            check(jc instanceof javax.tools.Tool, "compiler is a Tool");
            check(jc instanceof javax.tools.OptionChecker, "compiler is an OptionChecker");
            check("javac".equals(jc.name()), "compiler name javac");
        }

        javax.tools.DocumentationTool dt = javax.tools.ToolProvider.getSystemDocumentationTool();
        // A full JDK provides it; a non-full JDK may return null, which is legal.
        check(dt == null || dt instanceof javax.tools.Tool, "documentation tool is a Tool when present");

        ClassLoader cl = javax.tools.ToolProvider.getSystemToolClassLoader();
        check(cl == null || cl instanceof ClassLoader, "getSystemToolClassLoader returns null or a ClassLoader");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
