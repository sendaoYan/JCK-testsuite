/*
 * @test
 * @summary javax.tools.JavaCompiler.CompilationTask conformance: reflection shape checks.
 * @run main CompilationTaskTest
 */

import java.lang.reflect.*;

public class CompilationTaskTest {
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
        Class<?> c = Class.forName("javax.tools.JavaCompiler$CompilationTask");
        check(c != null, "javax.tools.JavaCompiler$CompilationTask loads");
        check(c.getName().equals("javax.tools.JavaCompiler$CompilationTask"), "binary name is javax.tools.JavaCompiler$CompilationTask");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.JavaCompiler$CompilationTask is public");
        check(c.isInterface(), "javax.tools.JavaCompiler$CompilationTask is an interface");
        check(ifaceNames(c).equals(set("java.util.concurrent.Callable")), "direct superinterfaces");
        check(declares(c, "setProcessors"), "declares setProcessors");
        check(declares(c, "setLocale"), "declares setLocale");
        check(declares(c, "call"), "declares call");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
