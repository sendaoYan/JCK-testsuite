/*
 * @test
 * @summary javax.lang.model.util.Elements conformance: reflection shape checks.
 * @run main ElementsTest
 */

import java.lang.reflect.*;

public class ElementsTest {
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
        Class<?> c = Class.forName("javax.lang.model.util.Elements");
        check(c != null, "javax.lang.model.util.Elements loads");
        check(c.getName().equals("javax.lang.model.util.Elements"), "binary name is javax.lang.model.util.Elements");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Elements is public");
        check(c.isInterface(), "javax.lang.model.util.Elements is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getPackageElement"), "declares getPackageElement()");
        check(declares(c, "getTypeElement"), "declares getTypeElement()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
