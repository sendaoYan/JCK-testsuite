/*
 * @test
 * @summary java.math.MathContext API conformance: full contract coverage.
 *          constants (UNLIMITED/DECIMAL32/64/128), constructors (int /
 *          int+RoundingMode / String), getPrecision/getRoundingMode, equals/
 *          hashCode/toString, serialization round-trip + serialVersionUID,
 *          and exact exception matrix (IllegalArgumentException / NPE).
 * @run main MathContextTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.math.MathContext;
import java.math.RoundingMode;

public class MathContextTest {
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

    static MathContext roundTrip(MathContext v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (MathContext) ois.readObject();
    }

    static void testConstants() {
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED precision");
        check(MathContext.UNLIMITED.getRoundingMode() == RoundingMode.HALF_UP, "UNLIMITED mode");
        check(MathContext.DECIMAL32.getPrecision() == 7, "DECIMAL32 precision");
        check(MathContext.DECIMAL32.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL32 mode");
        check(MathContext.DECIMAL64.getPrecision() == 16, "DECIMAL64 precision");
        check(MathContext.DECIMAL64.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL64 mode");
        check(MathContext.DECIMAL128.getPrecision() == 34, "DECIMAL128 precision");
        check(MathContext.DECIMAL128.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL128 mode");
    }

    static void testConstructors() {
        check(new MathContext(10).getPrecision() == 10, "(int) precision");
        check(new MathContext(10).getRoundingMode() == RoundingMode.HALF_UP, "(int) default mode");
        check(new MathContext(0).getPrecision() == 0, "(int) zero precision allowed");
        MathContext mc = new MathContext(10, RoundingMode.HALF_DOWN);
        check(mc.getPrecision() == 10 && mc.getRoundingMode() == RoundingMode.HALF_DOWN,
                "(int, RoundingMode)");
        MathContext s = new MathContext("precision=5 roundingMode=HALF_DOWN");
        check(s.getPrecision() == 5 && s.getRoundingMode() == RoundingMode.HALF_DOWN,
                "String constructor");
        check(new MathContext("precision=3 roundingMode=UP").getPrecision() == 3,
                "String precision");
    }

    static void testEqualsHashCodeToString() {
        check(new MathContext(10).equals(new MathContext(10)), "equals");
        check(!new MathContext(10).equals(new MathContext(11)), "equals diff precision");
        check(!new MathContext(10, RoundingMode.UP).equals(new MathContext(10, RoundingMode.DOWN)),
                "equals diff mode");
        check(new MathContext(10).hashCode() == new MathContext(10).hashCode(), "hashCode");
        check(new MathContext(10).equals(null) == false, "equals null");
        check(new MathContext(10).toString().equals("precision=10 roundingMode=HALF_UP"), "toString");
        check(new MathContext(5, RoundingMode.HALF_DOWN).toString()
                .equals("precision=5 roundingMode=HALF_DOWN"), "toString mode");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(MathContext.class).getSerialVersionUID() == 5579720004786848255L,
                "serialVersionUID");
        MathContext[] vals = { MathContext.UNLIMITED, MathContext.DECIMAL32,
                new MathContext(17, RoundingMode.DOWN) };
        for (MathContext v : vals) {
            MathContext r = roundTrip(v);
            check(r.equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(IllegalArgumentException.class, () -> new MathContext(-1), "negative precision");
        checkThrows(NullPointerException.class, () -> new MathContext(10, null), "null RoundingMode");
        checkThrows(NullPointerException.class, () -> new MathContext((String) null), "null String");
        checkThrows(IllegalArgumentException.class, () -> new MathContext("precision=5"),
                "String missing roundingMode");
        checkThrows(IllegalArgumentException.class, () -> new MathContext("precision=-1 roundingMode=UP"),
                "String negative precision");
        checkThrows(IllegalArgumentException.class, () -> new MathContext("garbage"), "bad String");
    }

    // ===== 契约深化（2026-11）：精确异常消息 + toString/解析往返 + 序列化 =====

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
        // 实测锁定：负精度消息为 "Digits < 0"（并非直觉的 "precision < 0"）
        checkMsg(IllegalArgumentException.class, "Digits < 0",
              () -> new MathContext(-1), "MC(-1) 消息逐字");
        checkMsg(IllegalArgumentException.class, "Digits < 0",
              () -> new MathContext(-1, RoundingMode.HALF_UP), "MC(-1,mode) 消息逐字");
        checkMsg(IllegalArgumentException.class, "Digits < 0",
              () -> new MathContext("precision=-1 roundingMode=UP"), "串负精度消息逐字");
        checkMsg(NullPointerException.class, "null RoundingMode",
              () -> new MathContext(10, null), "MC(10,null) 消息逐字");
        checkMsg(NullPointerException.class, "null String",
              () -> new MathContext((String) null), "MC(null串) 消息逐字");
        checkMsg(IllegalArgumentException.class, "bad string format",
              () -> new MathContext("garbage"), "乱串消息逐字");
        checkMsg(IllegalArgumentException.class, "bad string format",
              () -> new MathContext("precision=5"), "缺舍入模式消息逐字");
        checkMsg(IllegalArgumentException.class, "bad string format",
              () -> new MathContext("precision=abc roundingMode=HALF_UP"), "非数字精度消息逐字");
        checkMsg(IllegalArgumentException.class, "bad string format",
              () -> new MathContext("precision=5 roundingMode=BOGUS"), "未知舍入模式消息逐字");
        checkMsg(IllegalArgumentException.class, "bad string format",
              () -> new MathContext("roundingMode=HALF_UP"), "缺精度消息逐字");
    }

    static void testToStringParseMatrix() {
        check(MathContext.DECIMAL32.toString().equals("precision=7 roundingMode=HALF_EVEN"),
              "DECIMAL32 toString 全等");
        check(MathContext.DECIMAL64.toString().equals("precision=16 roundingMode=HALF_EVEN"),
              "DECIMAL64 toString 全等");
        check(MathContext.DECIMAL128.toString().equals("precision=34 roundingMode=HALF_EVEN"),
              "DECIMAL128 toString 全等");
        check(MathContext.UNLIMITED.toString().equals("precision=0 roundingMode=HALF_UP"),
              "UNLIMITED toString 全等");
        check(new MathContext(5).toString().equals("precision=5 roundingMode=HALF_UP"),
              "MC(5) toString 全等");
        // toString → String 构造 解析往返
        for (MathContext mc : new MathContext[]{ MathContext.DECIMAL32, MathContext.DECIMAL64,
                MathContext.DECIMAL128, MathContext.UNLIMITED, new MathContext(9, RoundingMode.DOWN) }) {
            MathContext parsed = new MathContext(mc.toString());
            check(parsed.equals(mc), "解析 toString 往返 " + mc);
            check(parsed.getPrecision() == mc.getPrecision(), "解析保留精度 " + mc);
            check(parsed.getRoundingMode() == mc.getRoundingMode(), "解析保留舍入模式 " + mc);
        }
        check(new MathContext("precision=0 roundingMode=UNNECESSARY").getRoundingMode()
                == RoundingMode.UNNECESSARY, "串构造 UNNECESSARY");
        check(new MathContext("precision=1 roundingMode=CEILING").getRoundingMode()
                == RoundingMode.CEILING, "串构造 CEILING");
        check(new MathContext("precision=1 roundingMode=FLOOR").getRoundingMode()
                == RoundingMode.FLOOR, "串构造 FLOOR");
        check(new MathContext("precision=1 roundingMode=UP").getRoundingMode()
                == RoundingMode.UP, "串构造 UP");
        check(new MathContext("precision=1 roundingMode=DOWN").getRoundingMode()
                == RoundingMode.DOWN, "串构造 DOWN");
        check(new MathContext("precision=1 roundingMode=HALF_DOWN").getRoundingMode()
                == RoundingMode.HALF_DOWN, "串构造 HALF_DOWN");
    }

    static void testEqualsHashSemantics() {
        check(MathContext.DECIMAL32.equals(new MathContext(7, RoundingMode.HALF_EVEN)),
              "DECIMAL32 equals new MC(7,HALF_EVEN)");
        check(MathContext.DECIMAL64.equals(new MathContext(16, RoundingMode.HALF_EVEN)),
              "DECIMAL64 equals new MC(16,HALF_EVEN)");
        check(MathContext.DECIMAL128.equals(new MathContext(34, RoundingMode.HALF_EVEN)),
              "DECIMAL128 equals new MC(34,HALF_EVEN)");
        check(MathContext.UNLIMITED.equals(new MathContext(0, RoundingMode.HALF_UP)),
              "UNLIMITED equals new MC(0,HALF_UP)");
        check(new MathContext(0).getPrecision() == 0, "MC(0) 精度 0 合法");
        check(new MathContext(0, RoundingMode.UNNECESSARY).getRoundingMode()
                == RoundingMode.UNNECESSARY, "MC(0,UNNECESSARY) 合法");
        MathContext a = new MathContext(12, RoundingMode.CEILING);
        MathContext b = new MathContext(12, RoundingMode.CEILING);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals ⇒ hashCode 相等");
        check(a.hashCode() == a.hashCode(), "hashCode 两次调用稳定");
        check(!a.equals(new MathContext(12, RoundingMode.FLOOR)), "仅舍入模式不同不等");
        check(!a.equals(new MathContext(13, RoundingMode.CEILING)), "仅精度不同不等");
        check(!a.equals(new Object()), "equals 与 Object false");
        // getRoundingMode 返回的是同一枚举实例
        check(MathContext.DECIMAL32.getRoundingMode() == RoundingMode.HALF_EVEN, "HALF_EVEN 同一枚举实例");
    }

    static void testSerializationExtra() throws Exception {
        MathContext[] vals = { MathContext.DECIMAL64, MathContext.DECIMAL128,
                new MathContext(1, RoundingMode.UNNECESSARY) };
        for (MathContext v : vals) {
            MathContext r = roundTrip(v);
            check(r.equals(v), "序列化 round-trip " + v);
            check(r.getPrecision() == v.getPrecision(), "序列化保留精度 " + v);
            check(r.getRoundingMode() == v.getRoundingMode(), "序列化保留舍入模式 " + v);
            check(r.toString().equals(v.toString()), "序列化 toString 相同 " + v);
        }
        // 与 BigDecimal 的联动契约
        java.math.BigDecimal v = new java.math.BigDecimal("123456789");
        check(v.round(MathContext.DECIMAL32).toString().equals("1.234568E+8"),
              "DECIMAL32 precision=7 生效于 round");
        check(v.round(MathContext.UNLIMITED).toString().equals("123456789"),
              "UNLIMITED precision=0 不舍入");
        check(v.round(new MathContext(4, RoundingMode.DOWN)).toString().equals("1.234E+8"),
              "MC(4,DOWN) round");
        check(new java.math.BigDecimal("2").pow(10, MathContext.DECIMAL32).toString().equals("1024"),
              "pow 与 MC 精确");
    }

    public static void main(String[] args) throws Exception {
        testConstants();
        testConstructors();
        testEqualsHashCodeToString();
        testSerialization();
        testErrors();
        testExactMessages();
        testToStringParseMatrix();
        testEqualsHashSemantics();
        testSerializationExtra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
