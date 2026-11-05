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
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 深化(2026-11-05)：声明序/ordinal 全表、valueOf round-trip、枚举通用契约（经 JDK8u372 实测）----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("jdk.net.SocketFlow$Status");
        // 1. values() 声明序全表（ordinal 0..7 精确对应）
        String[] order = {"NO_STATUS", "OK", "NO_PERMISSION", "NOT_CONNECTED",
                "NOT_SUPPORTED", "ALREADY_CREATED", "IN_PROGRESS", "OTHER"};
        Object[] vals = c.getEnumConstants();
        for (int i = 0; i < order.length; i++) {
            Enum<?> e = (Enum<?>) vals[i];
            check(e.ordinal() == i, order[i] + " ordinal " + i);
            check(order[i].equals(e.name()), order[i] + " name");
            check(order[i].equals(e.toString()), order[i] + " toString==name");
            check(e.getDeclaringClass() == c, order[i] + " declaring class");
        }
        // 2. values() 每次返回新克隆数组（元素同一性不变）
        Object[] v2 = c.getEnumConstants();
        check(vals != v2, "values returns fresh clone");
        check(vals.length == v2.length, "values length stable");
        check(vals[0] == v2[0], "clone elements identical");
        // 3. valueOf round-trip：valueOf(name()) 返回同一常量（同一性）
        for (Object o : vals) {
            Enum<?> e = (Enum<?>) o;
            check(Enum.valueOf((Class<? extends Enum>) c, e.name()) == e, "valueOf identity " + e.name());
        }
        // 4. valueOf 异常面：不存在 → IAE，null → NPE
        try { Enum.valueOf((Class<? extends Enum>) c, "NO_SUCH_STATUS"); check(false, "valueOf bogus should IAE"); }
        catch (IllegalArgumentException e) { check(true, "valueOf bogus IAE"); }
        try { Enum.valueOf((Class<? extends Enum>) c, null); check(false, "valueOf null should NPE"); }
        catch (NullPointerException e) { check(true, "valueOf null NPE"); }
        // 5. compareTo：按 ordinal 排序（raw 化绕开通配符捕获）
        @SuppressWarnings("unchecked")
        Enum<?> first = (Enum<?>) vals[0], last = (Enum<?>) vals[7], mid = (Enum<?>) vals[3];
        check(((Enum) first).compareTo(last) < 0, "first < last");
        check(((Enum) last).compareTo(first) > 0, "last > first");
        check(((Enum) first).compareTo(first) == 0, "self compareTo 0");
        check(((Enum) first).compareTo(mid) < 0, "NO_STATUS < NOT_CONNECTED");
        // 6. EnumSet 全集
        check(java.util.EnumSet.allOf((Class<? extends Enum>) c).size() == 8, "EnumSet.allOf size 8");
        // 7. equals/hashCode/clone 契约
        check(first.equals(first), "enum equals self identity");
        check(!first.equals(last), "enum constants distinct");
        check(first.hashCode() == first.hashCode(), "hashCode stable");
        check(first == vals[0], "getEnumConstants stable identity");
        // 8. 类结构：final 枚举
        check(Modifier.isFinal(c.getModifiers()), "Status is final");
        check(c.getDeclaredFields().length > 0, "has fields");
        // 9. 行为联动：SocketFlow.create().status() 即 NO_STATUS 常量
        check(jdk.net.SocketFlow.create().status() == jdk.net.SocketFlow.Status.NO_STATUS, "SocketFlow default status constant");
    }
}
