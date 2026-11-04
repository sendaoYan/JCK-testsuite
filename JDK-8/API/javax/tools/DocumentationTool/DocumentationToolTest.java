/*
 * @test
 * @summary javax.tools.DocumentationTool conformance: reflection shape checks.
 * @run main DocumentationToolTest
 */

import java.lang.reflect.*;

public class DocumentationToolTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool");
        check(c != null, "javax.tools.DocumentationTool loads");
        check(c.getName().equals("javax.tools.DocumentationTool"), "binary name is javax.tools.DocumentationTool");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.DocumentationTool is public");
        check(c.isInterface(), "javax.tools.DocumentationTool is an interface");
        check(ifaceNames(c).equals(set("javax.tools.Tool", "javax.tools.OptionChecker")), "direct superinterfaces");
        check(declares(c, "getTask"), "declares getTask");
        check(declares(c, "getStandardFileManager"), "declares getStandardFileManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
