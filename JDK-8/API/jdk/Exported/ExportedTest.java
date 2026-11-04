/*
 * @test
 * @summary jdk.Exported conformance: reflection shape checks.
 * @run main ExportedTest
 */

import java.lang.reflect.*;

public class ExportedTest {
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
        Class<?> c = Class.forName("jdk.Exported");
        check(c != null, "jdk.Exported loads");
        check(c.getName().equals("jdk.Exported"), "binary name is jdk.Exported");
        check(Modifier.isPublic(c.getModifiers()), "jdk.Exported is public");
        check(c.isInterface(), "jdk.Exported is an interface");
        check(c.isAnnotation(), "jdk.Exported is an annotation");
        check(ifaceNames(c).equals(set("java.lang.annotation.Annotation")), "direct superinterfaces");
        check(declares(c, "value"), "declares value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
