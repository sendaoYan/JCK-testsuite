/*
 * @test
 * @summary javax.tools.Tool conformance: white-box structure + black-box behavior.
 * @run main ToolTest
 */

import java.lang.reflect.*;
import java.io.ByteArrayOutputStream;

public class ToolTest {
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
        Class<?> c = Class.forName("javax.tools.Tool");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "name"), "declares name");
        check(hasMethod(c, "run"), "declares run");
        check(hasMethod(c, "getSourceVersions"), "declares getSourceVersions");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // JavaCompiler is a Tool; exercise its Tool methods.
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc != null) {
            javax.tools.Tool tool = jc;
            check("javac".equals(tool.name()), "javac name()");
            java.util.Set<javax.lang.model.SourceVersion> versions = tool.getSourceVersions();
            check(versions != null && !versions.isEmpty(), "javac source versions non-empty");
            check(versions.contains(javax.lang.model.SourceVersion.valueOf("RELEASE_27")),
                    "javac source versions contains RELEASE_27");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int r = tool.run(System.in, out, out, "-version");
            check(r == 0, "run -version returns 0");
            check(out.toString().contains("javac"), "run -version prints javac");
        }

        // DocumentationTool is also a Tool.
        javax.tools.DocumentationTool dt = javax.tools.ToolProvider.getSystemDocumentationTool();
        if (dt != null) {
            check("javadoc".equals(dt.name()), "javadoc name()");
            check(dt.getSourceVersions() != null && !dt.getSourceVersions().isEmpty(), "javadoc source versions non-empty");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
