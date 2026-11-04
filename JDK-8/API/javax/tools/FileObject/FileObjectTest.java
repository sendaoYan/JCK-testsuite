/*
 * @test
 * @summary javax.tools.FileObject conformance: reflection shape checks.
 * @run main FileObjectTest
 */

import java.lang.reflect.*;

public class FileObjectTest {
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
        Class<?> c = Class.forName("javax.tools.FileObject");
        check(c != null, "javax.tools.FileObject loads");
        check(c.getName().equals("javax.tools.FileObject"), "binary name is javax.tools.FileObject");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.FileObject is public");
        check(c.isInterface(), "javax.tools.FileObject is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "toUri"), "declares toUri");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "openInputStream"), "declares openInputStream");
        check(declares(c, "openOutputStream"), "declares openOutputStream");
        check(declares(c, "openReader"), "declares openReader");
        check(declares(c, "getCharContent"), "declares getCharContent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
