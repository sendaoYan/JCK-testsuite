/*
 * @test
 * @summary java.lang.NumberFormatException conformance: white-box structure + black-box behavior.
 * @run main NumberFormatExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NumberFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.NumberFormatException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.NumberFormatException");

        NumberFormatException e0 = new NumberFormatException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { NumberFormatException e = new NumberFormatException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        NumberFormatException t = new NumberFormatException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.NumberFormatException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: malformed numeric string
        expect(NumberFormatException.class, () -> { Integer.parseInt("not-a-number"); }, "trigger malformed numeric string");
    }

    // ================= 契约深化（2026-11）追加 =================

    /** true 当运行在 JDK 9+（版本串不再以 "1." 开头），用于锁定异常消息文本差异。 */
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    /** 捕获 NumberFormatException 并逐字比对消息文本（实测锁定）。 */
    static void checkNfeMsg(String expected, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected NumberFormatException with <" + expected + ">");
        } catch (NumberFormatException e) {
            check(expected.equals(e.getMessage()),
                  label + ": expected message <" + expected + "> but got <" + e.getMessage() + ">");
        }
    }

    static void testHierarchyFull() {
        NumberFormatException e = new NumberFormatException();
        check(e instanceof IllegalArgumentException, "NFE is an IllegalArgumentException");
        check(e instanceof RuntimeException, "NFE is a RuntimeException");
        check(e instanceof Exception, "NFE is an Exception");
        check(e instanceof Throwable, "NFE is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(NumberFormatException.class),
              "NFE is Serializable");
        check(NumberFormatException.class.getSuperclass() == IllegalArgumentException.class,
              "superclass is IllegalArgumentException");
        // 两侧均仅有 2 个公有构造（无 (String,Throwable) 形态，javap 实证）
        check(NumberFormatException.class.getDeclaredConstructors().length == 2,
              "exactly 2 declared constructors (no (String,Throwable) variant)");
    }

    static void testThrowableContract() {
        // toString 逐字锁定与消息透传（unicode、长文本）
        check(new NumberFormatException().toString().equals("java.lang.NumberFormatException"),
              "toString no-arg exact");
        check(new NumberFormatException("bad").toString().equals("java.lang.NumberFormatException: bad"),
              "toString with message exact");
        NumberFormatException u = new NumberFormatException("数值越界");
        check("数值越界".equals(u.getMessage()), "unicode message transparency");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append("x");
        NumberFormatException longMsg = new NumberFormatException(sb.toString());
        check(longMsg.getMessage().length() == 100, "long message transparent");
        // (String) 构造后 initCause 仍可补设（cause 未初始化时）
        NumberFormatException t2 = new NumberFormatException("m");
        Exception cause2 = new NumberFormatException("inner");
        t2.initCause(cause2);
        check(t2.getCause() == cause2, "initCause after (String) ctor");
        // fillInStackTrace 返回自身
        NumberFormatException t = new NumberFormatException("w");
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        // 栈顶帧属于本测试方法所在类（栈轨迹真实填充）
        check(t.getStackTrace().length >= 1
              && t.getStackTrace()[0].getClassName().equals(NumberFormatExceptionTest.class.getName()),
              "top stack frame is this class");
    }

    static void testThrownFamily() {
        // 各包装 parse 家族的 NFE 与精确消息（实测逐字锁定；null 消息两侧不同）
        checkNfeMsg("For input string: \"not-a-number\"",
              () -> Integer.parseInt("not-a-number"), "Integer.parseInt alpha");
        checkNfeMsg("For input string: \"z\"", () -> Long.parseLong("z"), "Long.parseLong alpha");
        checkNfeMsg("For input string: \"z\"", () -> Short.parseShort("z"), "Short.parseShort alpha");
        checkNfeMsg("For input string: \"z\"", () -> Byte.parseByte("z"), "Byte.parseByte alpha");
        // JDK 8 报 "null"，JDK 9+ 报 "Cannot parse null string"（版本差异锁定）
        checkNfeMsg(MODERN ? "Cannot parse null string" : "null",
              () -> Integer.parseInt(null), "Integer.parseInt(null) message");
        checkNfeMsg(MODERN ? "Cannot parse null string" : "null",
              () -> Long.parseLong(null), "Long.parseLong(null) message");
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Integer.parseInt("10", 1), "Integer.parseInt radix 1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX",
              () -> Integer.parseInt("10", 37), "Integer.parseInt radix 37");
        // JDK 9+：非 10 进制的失败消息追加 " under radix N" 后缀（8 无此后缀）
        checkNfeMsg(MODERN ? "For input string: \"z\" under radix 16" : "For input string: \"z\"",
              () -> Integer.parseInt("z", 16), "Integer.parseInt radix 16 suffix");
        checkNfeMsg("Value out of range. Value:\"32768\" Radix:10",
              () -> Short.parseShort("32768"), "Short.parseShort overflow message");
        checkNfeMsg("Value out of range. Value:\"128\" Radix:10",
              () -> Byte.parseByte("128"), "Byte.parseByte overflow message");
        checkNfeMsg("Zero length string", () -> Integer.decode(""), "Integer.decode empty message");
        checkNfeMsg("Illegal leading minus sign on unsigned string -1.",
              () -> Integer.parseUnsignedInt("-1"), "parseUnsignedInt minus message");
        checkNfeMsg("String value 4294967296 exceeds range of unsigned int.",
              () -> Integer.parseUnsignedInt("4294967296"), "parseUnsignedInt overflow message");
        // JDK 9+：parseInt(CharSequence,int,int,int) 的失败定位消息（8 无此重载；消息内含被解析子串）
        checkNfeMsg("Error at index 0 in: \"ab\"",
              () -> Integer.parseInt("abc", 0, 2, 10), "parseInt(CharSequence) alpha at index 0");
        // valueOf 家族转抛 NFE
        Runnable[] triggers = {
            () -> Integer.valueOf("x"),
            () -> Long.valueOf("x"),
            () -> Short.valueOf("x"),
            () -> Byte.valueOf("x"),
            () -> Integer.decode("0x"),
            () -> Long.decode("#"),
            () -> Integer.valueOf("10", 1),
        };
        String[] labels = {
            "Integer.valueOf(String)", "Long.valueOf(String)", "Short.valueOf(String)",
            "Byte.valueOf(String)", "Integer.decode(0x)", "Long.decode(#)",
            "Integer.valueOf(String,int) bad radix"
        };
        for (int i = 0; i < triggers.length; i++) {
            final Runnable r = triggers[i];
            expect(NumberFormatException.class, r, labels[i]);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // 契约深化（2026-11）
        testHierarchyFull();
        testThrowableContract();
        testThrownFamily();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
