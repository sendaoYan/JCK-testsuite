/*
 * @test
 * @summary java.util.logging.LoggingPermission conformance: white-box structure + black-box behavior.
 * @run main LoggingPermissionTest
 */

import java.lang.reflect.Modifier;
import java.security.BasicPermission;
import java.security.Permission;
import java.util.logging.LoggingPermission;

public class LoggingPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.LoggingPermission");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(Permission.class.isAssignableFrom(c), "is a Permission");
        check(c.getSuperclass() == BasicPermission.class, "direct superclass BasicPermission");
        check(c.getConstructor(String.class, String.class) != null, "(String,String) constructor");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName() +
                    " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but none thrown");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LoggingPermission p = new LoggingPermission("control", null);
        check("control".equals(p.getName()), "getName control");
        check("".equals(p.getActions()), "null actions -> empty string");

        LoggingPermission p2 = new LoggingPermission("control", "");
        check("".equals(p2.getActions()), "empty actions preserved");

        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(p2), "equal by name+class");
        check(p.hashCode() == p2.hashCode(), "equal permissions equal hashCode");
        check(p.equals(new LoggingPermission("control", null)), "equals same name");
        check(!p.equals(null), "equals null false");
        check(!p.equals("control"), "equals non-permission false");

        check(p.implies(new LoggingPermission("control", null)), "implies same name");
        check(p.toString().contains("control"), "toString contains name");

        // only the name "control" is valid; non-empty actions are rejected
        expectThrows(IllegalArgumentException.class,
                () -> new LoggingPermission("other", null), "name != control IAE");
        expectThrows(IllegalArgumentException.class,
                () -> new LoggingPermission("control", "read"), "non-empty actions IAE");
        expectThrows(NullPointerException.class,
                () -> new LoggingPermission(null, null), "null name NPE");
    }


    // ===================== 契约深化：仅 control 名合法/非 null actions 校验/implies-equals-hashCode 对称 =====================
    static void testContracts() {
        java.util.logging.LoggingPermission p = new java.util.logging.LoggingPermission("control", null);
        check(p.getName().equals("control"), "name is control");
        check("".equals(p.getActions()), "actions empty string");
        // 等价实例：implies/equals/hashCode 一致
        java.util.logging.LoggingPermission q = new java.util.logging.LoggingPermission("control", "");
        check(p.implies(q) && q.implies(p), "mutual implies");
        check(p.equals(q) && q.equals(p), "equals symmetric");
        check(p.hashCode() == q.hashCode(), "hashCode consistent with equals");
        check(!p.equals(null) && !p.equals("control"), "equals null/foreign false");
        // 非 control 名 IAE；actions 非 null IAE
        try {
            new java.util.logging.LoggingPermission("write", null);
            check(false, "bad name IAE");
        } catch (IllegalArgumentException e) { check(true, "bad name IAE"); }
        try {
            new java.util.logging.LoggingPermission("control", "read");
            check(false, "non-null actions IAE");
        } catch (IllegalArgumentException e) { check(true, "non-null actions IAE"); }
        // null name NPE
        try {
            new java.util.logging.LoggingPermission(null, null);
            check(false, "null name NPE");
        } catch (NullPointerException e) { check(true, "null name NPE"); }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
