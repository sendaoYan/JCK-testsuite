/*
 * @test
 * @summary java.math.RoundingMode enum conformance: full contract coverage.
 *          8 constants with stable ordinals, values() order, valueOf(int)
 *          mapping for every mode, valueOf(String), name()/toString, and exact
 *          exception matrix (IllegalArgumentException / NPE).
 * @run main RoundingModeTest
 */

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testConstants() {
        check(RoundingMode.values().length == 8, "8 constants");
        // documented ordinal order
        RoundingMode[] expected = { RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
                RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
                RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY };
        RoundingMode[] actual = RoundingMode.values();
        for (int i = 0; i < expected.length; i++) {
            check(actual[i] == expected[i], "ordinal " + i + " == " + expected[i]);
            check(actual[i].ordinal() == i, "ordinal() of " + expected[i]);
        }
    }

    static void testValueOfInt() {
        check(RoundingMode.valueOf(0) == RoundingMode.UP, "valueOf(0)");
        check(RoundingMode.valueOf(1) == RoundingMode.DOWN, "valueOf(1)");
        check(RoundingMode.valueOf(2) == RoundingMode.CEILING, "valueOf(2)");
        check(RoundingMode.valueOf(3) == RoundingMode.FLOOR, "valueOf(3)");
        check(RoundingMode.valueOf(4) == RoundingMode.HALF_UP, "valueOf(4)");
        check(RoundingMode.valueOf(5) == RoundingMode.HALF_DOWN, "valueOf(5)");
        check(RoundingMode.valueOf(6) == RoundingMode.HALF_EVEN, "valueOf(6)");
        check(RoundingMode.valueOf(7) == RoundingMode.UNNECESSARY, "valueOf(7)");
        check(RoundingMode.UP.ordinal() == 0 && RoundingMode.UNNECESSARY.ordinal() == 7,
                "ordinal bounds");
    }

    static void testValueOfString() {
        for (RoundingMode m : RoundingMode.values()) {
            check(RoundingMode.valueOf(m.name()) == m, "valueOf(String) " + m);
        }
        check(RoundingMode.valueOf("HALF_UP").toString().equals("HALF_UP"), "name/toString");
        check(RoundingMode.HALF_UP.name().equals("HALF_UP"), "name");
    }

    static void testErrors() {
        checkThrows(IllegalArgumentException.class, () -> RoundingMode.valueOf(-1), "valueOf(-1)");
        checkThrows(IllegalArgumentException.class, () -> RoundingMode.valueOf(8), "valueOf(8)");
        checkThrows(IllegalArgumentException.class, () -> RoundingMode.valueOf("FOO"),
                "valueOf bad name");
        checkThrows(NullPointerException.class, () -> RoundingMode.valueOf((String) null),
                "valueOf null String");
    }

    // ===== 契约深化（2026-11）：精确消息 + ROUND_* 全映射 + ±2.5 核心表 + 枚举契约 =====

    static void checkMsg(Class<? extends Throwable> expected, String message, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            if (!expected.isInstance(t)) {
                throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            if (t.getMessage() == null || !t.getMessage().equals(message)) {
                throw new AssertionError(msg + ": expected message \"" + message
                        + "\" but was \"" + t.getMessage() + "\"");
            }
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testExactMessages() {
        checkMsg(IllegalArgumentException.class, "argument out of range",
              () -> RoundingMode.valueOf(8), "valueOf(8) 消息逐字");
        checkMsg(IllegalArgumentException.class, "argument out of range",
              () -> RoundingMode.valueOf(-1), "valueOf(-1) 消息逐字");
        checkMsg(IllegalArgumentException.class, "argument out of range",
              () -> RoundingMode.valueOf(Integer.MAX_VALUE), "valueOf(MAX) 消息逐字");
        checkMsg(IllegalArgumentException.class,
              "No enum constant java.math.RoundingMode.FOO",
              () -> RoundingMode.valueOf("FOO"), "valueOf(FOO) 消息逐字");
        checkMsg(IllegalArgumentException.class,
              "No enum constant java.math.RoundingMode.half_up",
              () -> RoundingMode.valueOf("half_up"), "大小写敏感消息逐字");
        checkMsg(NullPointerException.class, "Name is null",
              () -> RoundingMode.valueOf((String) null), "valueOf(null) 消息逐字");
    }

    static void testLegacyConstantMapping() {
        // 旧 BigDecimal.ROUND_* 常量 → RoundingMode 全映射表（0..7）
        check(BigDecimal.ROUND_UP == 0, "ROUND_UP == 0");
        check(BigDecimal.ROUND_DOWN == 1, "ROUND_DOWN == 1");
        check(BigDecimal.ROUND_CEILING == 2, "ROUND_CEILING == 2");
        check(BigDecimal.ROUND_FLOOR == 3, "ROUND_FLOOR == 3");
        check(BigDecimal.ROUND_HALF_UP == 4, "ROUND_HALF_UP == 4");
        check(BigDecimal.ROUND_HALF_DOWN == 5, "ROUND_HALF_DOWN == 5");
        check(BigDecimal.ROUND_HALF_EVEN == 6, "ROUND_HALF_EVEN == 6");
        check(BigDecimal.ROUND_UNNECESSARY == 7, "ROUND_UNNECESSARY == 7");
        check(RoundingMode.valueOf(BigDecimal.ROUND_UP) == RoundingMode.UP, "of(ROUND_UP)==UP");
        check(RoundingMode.valueOf(BigDecimal.ROUND_DOWN) == RoundingMode.DOWN, "of(ROUND_DOWN)==DOWN");
        check(RoundingMode.valueOf(BigDecimal.ROUND_CEILING) == RoundingMode.CEILING, "of(ROUND_CEILING)==CEILING");
        check(RoundingMode.valueOf(BigDecimal.ROUND_FLOOR) == RoundingMode.FLOOR, "of(ROUND_FLOOR)==FLOOR");
        check(RoundingMode.valueOf(BigDecimal.ROUND_HALF_UP) == RoundingMode.HALF_UP, "of(ROUND_HALF_UP)==HALF_UP");
        check(RoundingMode.valueOf(BigDecimal.ROUND_HALF_DOWN) == RoundingMode.HALF_DOWN, "of(ROUND_HALF_DOWN)==HALF_DOWN");
        check(RoundingMode.valueOf(BigDecimal.ROUND_HALF_EVEN) == RoundingMode.HALF_EVEN, "of(ROUND_HALF_EVEN)==HALF_EVEN");
        check(RoundingMode.valueOf(BigDecimal.ROUND_UNNECESSARY) == RoundingMode.UNNECESSARY, "of(ROUND_UNNECESSARY)==UNNECESSARY");
    }

    static void testSetScaleCoreTable() {
        // 招牌核心表：setScale(0, mode) 于 ±2.5，8 模式全等（CEILING/FLOOR 关于 0 不对称）
        RoundingMode[] modes = { RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
                RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
                RoundingMode.HALF_EVEN };
        String[] pos = {"3", "2", "3", "2", "3", "2", "2"};
        String[] neg = {"-3", "-2", "-2", "-3", "-3", "-2", "-2"};
        for (int i = 0; i < modes.length; i++) {
            check(new java.math.BigDecimal("2.5").setScale(0, modes[i]).toString().equals(pos[i]),
                    "2.5 setScale(0," + modes[i] + ") == " + pos[i]);
            check(new java.math.BigDecimal("-2.5").setScale(0, modes[i]).toString().equals(neg[i]),
                    "-2.5 setScale(0," + modes[i] + ") == " + neg[i]);
        }
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new java.math.BigDecimal("2.5").setScale(0, RoundingMode.UNNECESSARY),
              "2.5 UNNECESSARY 消息逐字");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new java.math.BigDecimal("-2.5").setScale(0, RoundingMode.UNNECESSARY),
              "-2.5 UNNECESSARY 消息逐字");
        // HALF_EVEN 就偶表：3.5→4、4.5→4、5.5→6
        check(new java.math.BigDecimal("3.5").setScale(0, RoundingMode.HALF_EVEN).toString().equals("4"),
              "HALF_EVEN 3.5 → 4");
        check(new java.math.BigDecimal("4.5").setScale(0, RoundingMode.HALF_EVEN).toString().equals("4"),
              "HALF_EVEN 4.5 → 4（就偶）");
        check(new java.math.BigDecimal("5.5").setScale(0, RoundingMode.HALF_EVEN).toString().equals("6"),
              "HALF_EVEN 5.5 → 6");
        check(new java.math.BigDecimal("-3.5").setScale(0, RoundingMode.HALF_EVEN).toString().equals("-4"),
              "HALF_EVEN -3.5 → -4");
        // UP 与 DOWN 关于 0 对称
        check(new java.math.BigDecimal("-1.1").setScale(0, RoundingMode.UP).toString().equals("-2"),
              "UP -1.1 → -2（远离零）");
        check(new java.math.BigDecimal("-1.1").setScale(0, RoundingMode.DOWN).toString().equals("-1"),
              "DOWN -1.1 → -1（靠近零）");
        check(new java.math.BigDecimal("1.1").setScale(0, RoundingMode.CEILING).toString().equals("2"),
              "CEILING 1.1 → 2（向正无穷）");
        check(new java.math.BigDecimal("-1.1").setScale(0, RoundingMode.FLOOR).toString().equals("-2"),
              "FLOOR -1.1 → -2（向负无穷）");
        // UNNECESSARY 精确时恒等
        check(new java.math.BigDecimal("2.0").setScale(0, RoundingMode.UNNECESSARY).toString().equals("2"),
              "UNNECESSARY 精确 2.0 → 2");
    }

    static void testEnumContract() {
        RoundingMode[] all = RoundingMode.values();
        check(all != RoundingMode.values(), "values() 每次返回新数组");
        check(all.length == 8, "8 常量");
        // compareTo == ordinal 差
        check(RoundingMode.UP.compareTo(RoundingMode.UNNECESSARY) == -7, "UP vs UNNECESSARY == -7");
        check(RoundingMode.HALF_UP.compareTo(RoundingMode.UP) == 4, "HALF_UP vs UP == 4");
        check(RoundingMode.HALF_EVEN.compareTo(RoundingMode.HALF_EVEN) == 0, "自身 compareTo 0");
        // name/toString 恒等且 valueOf 往返同一实例（枚举单例）
        for (RoundingMode m : RoundingMode.values()) {
            check(RoundingMode.valueOf(m.name()) == m, "valueOf(name) 同一实例 " + m);
            check(m.toString().equals(m.name()), "toString == name " + m);
        }
        check(Enum.valueOf(RoundingMode.class, "FLOOR") == RoundingMode.FLOOR,
              "Enum.valueOf 泛型形态");
        check(RoundingMode.HALF_EVEN.getDeclaringClass() == RoundingMode.class,
              "getDeclaringClass");
        // 序数顺序即文档顺序
        check(RoundingMode.values()[4] == RoundingMode.HALF_UP, "第 5 个常量 HALF_UP");
        check(RoundingMode.UNNECESSARY.ordinal() == 7, "UNNECESSARY 序数 7");
    }

    public static void main(String[] args) {
        testConstants();
        testValueOfInt();
        testValueOfString();
        testErrors();
        testExactMessages();
        testLegacyConstantMapping();
        testSetScaleCoreTable();
        testEnumContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
