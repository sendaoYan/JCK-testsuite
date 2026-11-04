/*
 * @test
 * @summary javax.tools.DocumentationTool conformance: white-box structure + black-box behavior.
 * @run main DocumentationToolTest
 */

import java.lang.reflect.*;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

public class DocumentationToolTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 2, "2 superinterface(s)");
        check(hasInterface(c, "javax.tools.Tool"), "extends javax.tools.Tool");
        check(hasInterface(c, "javax.tools.OptionChecker"), "extends javax.tools.OptionChecker");
        check(hasMethod(c, "getTask"), "declares getTask");
        check(hasMethod(c, "getStandardFileManager"), "declares getStandardFileManager");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A full JDK provides the documentation tool; a non-full JDK returns null.
        javax.tools.DocumentationTool dt = javax.tools.ToolProvider.getSystemDocumentationTool();
        if (dt == null) {
            // Degrade: only structural assertions already covered above.
            return;
        }
        check("javadoc".equals(dt.name()), "name() == javadoc");
        check(dt.getSourceVersions() != null && !dt.getSourceVersions().isEmpty(), "source versions non-empty");
        check(dt.getSourceVersions().contains(javax.lang.model.SourceVersion.valueOf("RELEASE_27")),
                "source versions contains RELEASE_27");

        javax.tools.StandardJavaFileManager fm =
                dt.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        check(fm != null, "getStandardFileManager non-null");
        fm.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
