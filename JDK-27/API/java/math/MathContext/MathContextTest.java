/*
 * @test
 * @summary java.math.MathContext conformance: white-box structure + black-box behavior
 *          (precision/rounding-mode getters, DECIMAL32/64/128/UNLIMITED constants,
 *           (int)/(int,RoundingMode)/(String) constructors, equals/hashCode/toString round-trip,
 *           IllegalArgumentException/NullPointerException on invalid inputs).
 * @run main MathContextTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public class MathContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static MathContext roundTrip(MathContext v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (MathContext) ois.readObject();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.math.MathContext");

        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()),
              "MathContext is public final");
        check(!c.isInterface() && !c.isEnum(), "MathContext is a concrete class");
        check(Arrays.asList(c.getInterfaces()).contains(Serializable.class),
              "MathContext implements Serializable");

        for (String f : new String[]{"UNLIMITED", "DECIMAL32", "DECIMAL64", "DECIMAL128"}) {
            Field field = c.getField(f);
            check(Modifier.isPublic(field.getModifiers())
                  && Modifier.isStatic(field.getModifiers())
                  && Modifier.isFinal(field.getModifiers())
                  && field.getType() == MathContext.class,
                  "static final MathContext " + f);
        }

        String[][] methods = {
            {"getPrecision"},
            {"getRoundingMode"},
            {"equals", "java.lang.Object"},
            {"hashCode"},
            {"toString"},
        };
        for (String[] m : methods) {
            Class<?>[] params = (m.length == 1) ? new Class<?>[0] : new Class<?>[]{Class.forName(m[1])};
            Method method = c.getMethod(m[0], params);
            check(Modifier.isPublic(method.getModifiers()), "public method " + m[0]);
        }
        // constructors
        check(c.getConstructor(int.class) != null, "constructor (int)");
        check(c.getConstructor(int.class, RoundingMode.class) != null, "constructor (int, RoundingMode)");
        check(c.getConstructor(String.class) != null, "constructor (String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        constants();
        constructors();
        getters();
        equalityAndString();
        exceptions();
        testSerialization();
    }

    static void constants() {
        check(MathContext.DECIMAL32.getPrecision() == 7, "DECIMAL32 precision 7");
        check(MathContext.DECIMAL64.getPrecision() == 16, "DECIMAL64 precision 16");
        check(MathContext.DECIMAL128.getPrecision() == 34, "DECIMAL128 precision 34");
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED precision 0");
        check(MathContext.DECIMAL32.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL32 HALF_EVEN");
        check(MathContext.DECIMAL64.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL64 HALF_EVEN");
        check(MathContext.DECIMAL128.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL128 HALF_EVEN");
        check(MathContext.UNLIMITED.getRoundingMode() == RoundingMode.HALF_UP, "UNLIMITED HALF_UP");
    }

    static void constructors() {
        check(new MathContext(10).getPrecision() == 10, "(int) ctor precision");
        check(new MathContext(10).getRoundingMode() == RoundingMode.HALF_UP,
              "(int) ctor default rounding HALF_UP");
        check(new MathContext(0).getPrecision() == 0, "(int) ctor zero precision allowed");
        check(new MathContext(10, RoundingMode.HALF_EVEN).getPrecision() == 10, "(int, mode) precision");
        check(new MathContext(10, RoundingMode.HALF_EVEN).getRoundingMode() == RoundingMode.HALF_EVEN,
              "(int, mode) rounding mode");
        // String constructor parses toString() output
        MathContext parsed = new MathContext("precision=9 roundingMode=HALF_UP");
        check(parsed.getPrecision() == 9 && parsed.getRoundingMode() == RoundingMode.HALF_UP,
              "String ctor round-trip");
        check(new MathContext("precision=0 roundingMode=HALF_UP").getPrecision() == 0,
              "String ctor zero precision");
    }

    static void getters() {
        MathContext mc = new MathContext(12, RoundingMode.CEILING);
        check(mc.getPrecision() == 12, "getPrecision");
        check(mc.getRoundingMode() == RoundingMode.CEILING, "getRoundingMode");
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED getPrecision");
    }

    static void equalityAndString() {
        check(new MathContext(10, RoundingMode.HALF_UP).equals(new MathContext(10, RoundingMode.HALF_UP)),
              "equals same settings");
        check(!new MathContext(10, RoundingMode.HALF_UP).equals(new MathContext(10, RoundingMode.HALF_EVEN)),
              "equals different rounding");
        check(!new MathContext(10).equals(new MathContext(11)), "equals different precision");
        check(!new MathContext(10).equals("10"), "equals against String false");
        check(!new MathContext(10).equals(null), "equals null false");
        check(new MathContext(10, RoundingMode.HALF_UP).hashCode()
              == new MathContext(10, RoundingMode.HALF_UP).hashCode(), "equal objects equal hashCode");
        check(MathContext.DECIMAL32.toString().equals("precision=7 roundingMode=HALF_EVEN"),
              "DECIMAL32 toString");
        check(MathContext.UNLIMITED.toString().equals("precision=0 roundingMode=HALF_UP"),
              "UNLIMITED toString");
        check(new MathContext(9, RoundingMode.HALF_UP).toString()
              .equals("precision=9 roundingMode=HALF_UP"), "toString format");
        check(new MathContext(7, RoundingMode.HALF_EVEN).equals(MathContext.DECIMAL32),
              "DECIMAL32 equals new MathContext(7, HALF_EVEN)");
        check(MathContext.DECIMAL64.toString().equals("precision=16 roundingMode=HALF_EVEN"),
              "DECIMAL64 toString");
        check(MathContext.DECIMAL128.toString().equals("precision=34 roundingMode=HALF_EVEN"),
              "DECIMAL128 toString");
        check(MathContext.UNLIMITED.equals(new MathContext(0, RoundingMode.HALF_UP)),
              "UNLIMITED equals new MathContext(0, HALF_UP)");
        // round-trip: parse toString() of arbitrary context
        MathContext orig = new MathContext(5, RoundingMode.DOWN);
        MathContext copy = new MathContext(orig.toString());
        check(copy.equals(orig), "toString/parse round-trip");
    }

    static void exceptions() {
        expectIllegalArgument(() -> new MathContext(-1), "negative precision");
        expectIllegalArgument(() -> new MathContext(-1, RoundingMode.HALF_UP), "negative precision with mode");
        expectNullPointer(() -> new MathContext(10, (RoundingMode) null), "null rounding mode");
        expectNullPointer(() -> new MathContext((String) null), "null String");
        expectIllegalArgument(() -> new MathContext("bad"), "bad String format");
        expectIllegalArgument(() -> new MathContext("precision=-1 roundingMode=HALF_UP"),
              "String with negative precision");
        expectIllegalArgument(() -> new MathContext("precision=abc roundingMode=HALF_UP"),
              "String with non-numeric precision");
        expectIllegalArgument(() -> new MathContext("precision=5 roundingMode=BOGUS"),
              "String with unknown rounding mode");
    }

    static void expectIllegalArgument(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected IllegalArgumentException: " + msg);
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void expectNullPointer(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected NullPointerException: " + msg);
        } catch (NullPointerException expected) {
            passed++;
        }
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

    // ===== 契约深化（2026-11）：精确异常消息 + toString/解析往返 + 序列化 =====

    static void checkMsg(Class<? extends Throwable> expected, String message, Runnable r, String msg) {
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
        // 实测锁定：负精度消息为 "Digits < 0"（并非直觉的 "precision < 0"；两侧一致）
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
        testWhiteBox();
        testBlackBox();
        testExactMessages();
        testToStringParseMatrix();
        testEqualsHashSemantics();
        testSerializationExtra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
