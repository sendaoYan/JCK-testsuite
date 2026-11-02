/*
 * @test
 * @summary javax.lang.model.UnknownEntityException conformance: reflection shape checks.
 * @run main UnknownEntityExceptionTest
 */

import java.lang.reflect.*;

public class UnknownEntityExceptionTest {
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
        Class<?> c = Class.forName("javax.lang.model.UnknownEntityException");
        check(c != null, "javax.lang.model.UnknownEntityException loads");
        check(c.getName().equals("javax.lang.model.UnknownEntityException"), "binary name is javax.lang.model.UnknownEntityException");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.UnknownEntityException is public");
        check(!c.isInterface(), "javax.lang.model.UnknownEntityException is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "javax.lang.model.UnknownEntityException is concrete");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass is java.lang.RuntimeException");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
