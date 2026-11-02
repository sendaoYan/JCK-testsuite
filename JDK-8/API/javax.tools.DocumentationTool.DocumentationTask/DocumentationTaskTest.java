/*
 * @test
 * @summary javax.tools.DocumentationTool.DocumentationTask conformance: reflection shape checks.
 * @run main DocumentationTaskTest
 */

import java.lang.reflect.*;

public class DocumentationTaskTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool$DocumentationTask");
        check(c != null, "javax.tools.DocumentationTool$DocumentationTask loads");
        check(c.getName().equals("javax.tools.DocumentationTool$DocumentationTask"), "binary name is javax.tools.DocumentationTool$DocumentationTask");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.DocumentationTool$DocumentationTask is public");
        check(c.isInterface(), "javax.tools.DocumentationTool$DocumentationTask is an interface");
        check(ifaceNames(c).equals(set("java.util.concurrent.Callable")), "direct superinterfaces");
        check(declares(c, "setLocale"), "declares setLocale");
        check(declares(c, "call"), "declares call");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
