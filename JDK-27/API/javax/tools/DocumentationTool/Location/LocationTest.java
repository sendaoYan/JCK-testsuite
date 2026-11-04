/*
 * @test
 * @summary javax.tools.DocumentationTool.Location conformance: white-box structure + black-box behavior.
 * @run main LocationTest
 */

import java.lang.reflect.*;

public class LocationTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool$Location");
        check(c.isEnum(), "isEnum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 4, "4 enum constants");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasInterface(c, "javax.tools.JavaFileManager$Location"), "implements javax.tools.JavaFileManager$Location");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "isOutputLocation"), "declares isOutputLocation");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.tools.DocumentationTool.Location[] all = javax.tools.DocumentationTool.Location.values();
        check(all.length == 4, "values() length 4");
        check(all[0] == javax.tools.DocumentationTool.Location.DOCUMENTATION_OUTPUT, "order DOCUMENTATION_OUTPUT");
        check(all[1] == javax.tools.DocumentationTool.Location.DOCLET_PATH, "order DOCLET_PATH");
        check(all[2] == javax.tools.DocumentationTool.Location.TAGLET_PATH, "order TAGLET_PATH");
        check(all[3] == javax.tools.DocumentationTool.Location.SNIPPET_PATH, "order SNIPPET_PATH");

        check("DOCUMENTATION_OUTPUT".equals(javax.tools.DocumentationTool.Location.DOCUMENTATION_OUTPUT.getName()),
                "getName DOCUMENTATION_OUTPUT");
        check("DOCLET_PATH".equals(javax.tools.DocumentationTool.Location.DOCLET_PATH.getName()), "getName DOCLET_PATH");
        check("TAGLET_PATH".equals(javax.tools.DocumentationTool.Location.TAGLET_PATH.getName()), "getName TAGLET_PATH");
        check("SNIPPET_PATH".equals(javax.tools.DocumentationTool.Location.SNIPPET_PATH.getName()), "getName SNIPPET_PATH");

        check(javax.tools.DocumentationTool.Location.DOCUMENTATION_OUTPUT.isOutputLocation(), "DOCUMENTATION_OUTPUT is output");
        check(!javax.tools.DocumentationTool.Location.DOCLET_PATH.isOutputLocation(), "DOCLET_PATH not output");
        check(!javax.tools.DocumentationTool.Location.TAGLET_PATH.isOutputLocation(), "TAGLET_PATH not output");
        check(!javax.tools.DocumentationTool.Location.SNIPPET_PATH.isOutputLocation(), "SNIPPET_PATH not output");

        check(javax.tools.DocumentationTool.Location.valueOf("SNIPPET_PATH")
                == javax.tools.DocumentationTool.Location.SNIPPET_PATH, "valueOf SNIPPET_PATH");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
