/*
 * @test
 * @summary javax.lang.model.util.ElementFilter conformance: reflection shape checks.
 * @run main ElementFilterTest
 */

import java.lang.reflect.*;

public class ElementFilterTest {
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
        Class<?> c = Class.forName("javax.lang.model.util.ElementFilter");
        check(c != null, "javax.lang.model.util.ElementFilter loads");
        check(c.getName().equals("javax.lang.model.util.ElementFilter"), "binary name is javax.lang.model.util.ElementFilter");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.ElementFilter is public");
        check(!c.isInterface(), "javax.lang.model.util.ElementFilter is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "javax.lang.model.util.ElementFilter is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
