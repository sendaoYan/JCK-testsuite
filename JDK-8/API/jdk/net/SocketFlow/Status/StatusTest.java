/*
 * @test
 * @summary jdk.net.SocketFlow.Status conformance: reflection shape checks.
 * @run main StatusTest
 */

import java.lang.reflect.*;

public class StatusTest {
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
        Class<?> c = Class.forName("jdk.net.SocketFlow$Status");
        check(c != null, "jdk.net.SocketFlow$Status loads");
        check(c.getName().equals("jdk.net.SocketFlow$Status"), "binary name is jdk.net.SocketFlow$Status");
        check(Modifier.isPublic(c.getModifiers()), "jdk.net.SocketFlow$Status is public");
        check(c.isEnum(), "jdk.net.SocketFlow$Status is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 8, "8 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(set("NO_STATUS", "OK", "NO_PERMISSION", "NOT_CONNECTED", "NOT_SUPPORTED", "ALREADY_CREATED", "IN_PROGRESS", "OTHER")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "NO_STATUS") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
