/*
 * @test
 * @summary com.sun.source.util.TaskListener conformance: reflection shape checks.
 * @run main TaskListenerTest
 */

import java.lang.reflect.*;

public class TaskListenerTest {
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
        Class<?> c = Class.forName("com.sun.source.util.TaskListener");
        check(c != null, "com.sun.source.util.TaskListener loads");
        check(c.getName().equals("com.sun.source.util.TaskListener"), "binary name is com.sun.source.util.TaskListener");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.TaskListener is public");
        check(c.isInterface(), "com.sun.source.util.TaskListener is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
