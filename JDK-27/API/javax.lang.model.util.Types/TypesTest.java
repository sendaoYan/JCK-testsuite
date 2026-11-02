/*
 * @test
 * @summary javax.lang.model.util.Types conformance: reflection shape checks.
 * @run main TypesTest
 */

import java.lang.reflect.*;

public class TypesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.lang.model.util.Types");
        check(c != null, "javax.lang.model.util.Types loads");
        check(c.getName().equals("javax.lang.model.util.Types"), "binary name is javax.lang.model.util.Types");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Types is public");
        check(c.isInterface(), "javax.lang.model.util.Types is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "asElement"), "declares asElement()");
        check(declares(c, "isSubtype"), "declares isSubtype()");
        check(declares(c, "isSameType"), "declares isSameType()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
