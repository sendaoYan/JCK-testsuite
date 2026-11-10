/*
 * @test
 * @summary java.util.logging.Level conformance: white-box structure + black-box behavior.
 * @run main LevelTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

public class LevelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.Level");
        check(c != null, "Level loadable");
        check(Modifier.isPublic(c.getModifiers()), "Level is public");
        check(!Modifier.isAbstract(c.getModifiers()), "Level is concrete");
        check(Serializable.class.isAssignableFrom(c), "Level implements Serializable");
        check(c.getSuperclass() == Object.class, "Level direct superclass is Object");

        check(Modifier.isPublic(c.getField("OFF").getModifiers())
                && Modifier.isStatic(c.getField("OFF").getModifiers())
                && Modifier.isFinal(c.getField("OFF").getModifiers()), "OFF is public static final");
        check(c.getField("SEVERE") != null && c.getField("ALL") != null, "standard constants present");

        check(hasMethod(c, "getName"), "getName()");
        check(hasMethod(c, "intValue"), "intValue()");
        check(hasMethod(c, "toString"), "toString()");
        check(hasMethod(c, "equals", Object.class), "equals(Object)");
        check(hasMethod(c, "hashCode"), "hashCode()");
        check(hasMethod(c, "getLocalizedName"), "getLocalizedName()");
        check(hasMethod(c, "getResourceBundleName"), "getResourceBundleName()");
        check(Modifier.isStatic(c.getMethod("parse", String.class).getModifiers()), "parse is static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // standard constant ordering (severity descends)
        check(Level.OFF.intValue() == Integer.MAX_VALUE, "OFF intValue");
        check(Level.SEVERE.intValue() == 1000, "SEVERE intValue");
        check(Level.WARNING.intValue() == 900, "WARNING intValue");
        check(Level.INFO.intValue() == 800, "INFO intValue");
        check(Level.CONFIG.intValue() == 700, "CONFIG intValue");
        check(Level.FINE.intValue() == 500, "FINE intValue");
        check(Level.FINER.intValue() == 400, "FINER intValue");
        check(Level.FINEST.intValue() == 300, "FINEST intValue");
        check(Level.ALL.intValue() == Integer.MIN_VALUE, "ALL intValue");
        check(Level.OFF.intValue() > Level.SEVERE.intValue()
                && Level.SEVERE.intValue() > Level.WARNING.intValue()
                && Level.WARNING.intValue() > Level.INFO.intValue()
                && Level.INFO.intValue() > Level.CONFIG.intValue()
                && Level.CONFIG.intValue() > Level.FINE.intValue()
                && Level.FINE.intValue() > Level.FINER.intValue()
                && Level.FINER.intValue() > Level.FINEST.intValue()
                && Level.FINEST.intValue() > Level.ALL.intValue(), "intValue ordering");

        // name / toString
        check("WARNING".equals(Level.WARNING.getName()), "getName WARNING");
        check("WARNING".equals(Level.WARNING.toString()), "toString WARNING");
        check("INFO".equals(Level.INFO.getName()), "getName INFO");
        check(Level.WARNING.getLocalizedName() != null, "getLocalizedName non-null");
        check(Level.INFO.getResourceBundleName() != null, "standard level has resource bundle name");

        // parse round-trips
        check(Level.parse("INFO") == Level.INFO, "parse name -> cached constant");
        check(Level.parse("WARNING") == Level.WARNING, "parse WARNING");
        check(Level.parse("800") == Level.INFO, "parse int value -> cached constant");
        check(Level.parse("900") == Level.WARNING, "parse 900");
        expectThrows(IllegalArgumentException.class, () -> Level.parse("bogus"), "parse bogus");
        expectThrows(IllegalArgumentException.class, () -> Level.parse(""), "parse empty");

        // equals / hashCode (equality by intValue)
        check(Level.INFO.equals(Level.parse("800")), "equals by value");
        check(!Level.INFO.equals(Level.WARNING), "different value not equal");
        check(!Level.INFO.equals(null), "equals null false");
        check(!Level.INFO.equals("INFO"), "equals non-Level false");
        check(Level.INFO.hashCode() == Level.parse("800").hashCode(), "equal levels equal hashCode");

        // custom subclass (protected constructors)
        MyLevel custom = new MyLevel("CUSTOM", 350);
        check("CUSTOM".equals(custom.getName()), "custom getName");
        check(custom.intValue() == 350, "custom intValue");
        check("CUSTOM".equals(custom.toString()), "custom toString");
        check(custom.getResourceBundleName() == null, "custom no bundle");
        MyLevel sameValue = new MyLevel("OTHER", 350);
        check(custom.equals(sameValue), "custom equals by value");
    }

    static final class MyLevel extends Level {
        MyLevel(String name, int value) { super(name, value); }
    }

    static final class MyNamedLevel extends Level {
        MyNamedLevel(String name, int value, String bundleName) { super(name, value, bundleName); }
    }


    // ===================== 契约深化：parse 矩阵/整型值映射/比较序/null 契约 =====================
    static void testContracts() {
        // 全部已知常量按名解析
        java.util.logging.Level[] all = {java.util.logging.Level.ALL, java.util.logging.Level.CONFIG,
              java.util.logging.Level.FINE, java.util.logging.Level.FINER, java.util.logging.Level.FINEST,
              java.util.logging.Level.INFO, java.util.logging.Level.OFF, java.util.logging.Level.SEVERE,
              java.util.logging.Level.WARNING};
        for (java.util.logging.Level lv : all)
            check(java.util.logging.Level.parse(lv.getName()) == lv, "parse(" + lv.getName() + ") identity");
        // 整型值表（锁定）
        check(java.util.logging.Level.ALL.intValue() == Integer.MIN_VALUE, "ALL = MIN_VALUE");
        check(java.util.logging.Level.FINEST.intValue() == 300 && java.util.logging.Level.FINER.intValue() == 400
              && java.util.logging.Level.FINE.intValue() == 500 && java.util.logging.Level.CONFIG.intValue() == 700
              && java.util.logging.Level.INFO.intValue() == 800 && java.util.logging.Level.WARNING.intValue() == 900
              && java.util.logging.Level.SEVERE.intValue() == 1000 && java.util.logging.Level.OFF.intValue() == Integer.MAX_VALUE,
              "standard int values table");
        // 数字串解析到已知整型值（800 -> INFO）
        check(java.util.logging.Level.parse("800") == java.util.logging.Level.INFO, "parse(800) = INFO");
        check(java.util.logging.Level.parse("1000") == java.util.logging.Level.SEVERE, "parse(1000) = SEVERE");
        // 负例：小写/未知/空串 -> IAE（消息含原串）
        for (String bad : new String[] {"info", "Bad level", "", "WARNING "}) {
            try {
                java.util.logging.Level.parse(bad);
                check(false, "parse(" + bad + ") IAE");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null && e.getMessage().contains(bad),
                      "parse bad message echoes input: " + e.getMessage());
            }
        }
        try {
            java.util.logging.Level.parse(null);
            check(false, "parse(null) NPE");
        } catch (NullPointerException e) { check(true, "parse(null) NPE"); }
        // equals/hashCode 一致性 + Comparable 序
        check(java.util.logging.Level.INFO.equals(java.util.logging.Level.parse("INFO"))
              && java.util.logging.Level.INFO.hashCode() == java.util.logging.Level.parse("INFO").hashCode(),
              "equals/hashCode consistent");
        check(java.util.logging.Level.FINEST.intValue() < java.util.logging.Level.SEVERE.intValue()
              && java.util.logging.Level.SEVERE.intValue() > java.util.logging.Level.FINEST.intValue()
              && java.util.logging.Level.INFO.intValue() == java.util.logging.Level.parse("800").intValue(),
              "ordering by intValue (Level is not Comparable)");
        // toString 返回名字
        check("INFO".equals(java.util.logging.Level.INFO.toString()), "toString is name");

        // 数字串解析：整型极值映射到 ALL/OFF；非标准值生成新 Level（非缓存、按值相等）
        check(java.util.logging.Level.parse("-2147483648") == Level.ALL, "parse(MIN_VALUE) = ALL");
        check(java.util.logging.Level.parse("2147483647") == Level.OFF, "parse(MAX_VALUE) = OFF");
        Level custom350 = Level.parse("350");
        check("350".equals(custom350.getName()) && custom350.intValue() == 350
              && custom350.getResourceBundleName() == null, "parse(350) makes custom level");
        check("350".equals(custom350.toString()), "parse(350) toString is name");
        check(custom350.equals(Level.parse("350")), "parse(350) equal by value on repeated call");
        check(custom350.equals(new MyLevel("350", 350)), "custom level equals independently-built same value");
        // 越界数字串 -> IAE
        try {
            Level.parse("2147483648");
            check(false, "parse overflow IAE");
        } catch (IllegalArgumentException e) { check(true, "parse overflow IAE"); }
        // 构造契约：null name NPE；空 name 允许；3-arg 保存 bundle 名；无 bundle 的 localized 名=名字
        try {
            new MyLevel(null, 5);
            check(false, "Level(null name) NPE");
        } catch (NullPointerException e) { check(true, "Level(null name) NPE"); }
        check("my.bundle".equals(new MyNamedLevel("NAMED", 123, "my.bundle").getResourceBundleName()),
              "3-arg ctor stores bundle name");
        check(new MyNamedLevel("NAMED", 123, null).getResourceBundleName() == null,
              "3-arg ctor null bundle name");
        check("CUSTOM".equals(new MyLevel("CUSTOM", 350).getLocalizedName()),
              "custom (no bundle) localized name = name");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
