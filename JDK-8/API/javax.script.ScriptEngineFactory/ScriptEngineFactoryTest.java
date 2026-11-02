/*
 * @test
 * @summary javax.script.ScriptEngineFactory conformance: reflection shape checks.
 * @run main ScriptEngineFactoryTest
 */

import java.lang.reflect.*;

public class ScriptEngineFactoryTest {
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
        Class<?> c = Class.forName("javax.script.ScriptEngineFactory");
        check(c != null, "javax.script.ScriptEngineFactory loads");
        check(c.getName().equals("javax.script.ScriptEngineFactory"), "binary name is javax.script.ScriptEngineFactory");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.ScriptEngineFactory is public");
        check(c.isInterface(), "javax.script.ScriptEngineFactory is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getEngineName"), "declares getEngineName");
        check(declares(c, "getEngineVersion"), "declares getEngineVersion");
        check(declares(c, "getExtensions"), "declares getExtensions");
        check(declares(c, "getMimeTypes"), "declares getMimeTypes");
        check(declares(c, "getNames"), "declares getNames");
        check(declares(c, "getLanguageName"), "declares getLanguageName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
