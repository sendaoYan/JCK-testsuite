/*
 * @test
 * @summary java.lang.NumberFormatException API conformance: IllegalArgumentException
 *          hierarchy, no-arg and (String) constructors, and Integer.parseInt throwing NFE
 *          (including the null-input case, which is NFE rather than NPE).
 * @run main NumberFormatExceptionTest
 */

public class NumberFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NumberFormatException e = new NumberFormatException();
        check(e instanceof IllegalArgumentException, "NFE is an IllegalArgumentException");
        check(e instanceof RuntimeException, "NFE is a RuntimeException");
    }

    static void testConstructors() {
        NumberFormatException e1 = new NumberFormatException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NumberFormatException e2 = new NumberFormatException("bad");
        check("bad".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        try {
            Integer.parseInt("not-a-number");
            throw new AssertionError("expected NumberFormatException");
        } catch (NumberFormatException e) {
            check("For input string: \"not-a-number\"".equals(e.getMessage()), "parseInt message");
        }
        try {
            Integer.parseInt(null);
            throw new AssertionError("parseInt(null): expected NumberFormatException");
        } catch (NumberFormatException e) {
            check(e instanceof NumberFormatException, "parseInt(null) throws NFE (not NPE)");
        }
    }

    // ================= 契约深化（2026-11）追加 =================

    /** true 当运行在 JDK 9+（版本串不再以 "1." 开头），用于锁定异常消息文本差异。 */
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    /** 捕获 NumberFormatException 并逐字比对消息文本（双侧实测锁定）。 */
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
        try {
            check(NumberFormatException.class.getSuperclass() == IllegalArgumentException.class,
                  "superclass is IllegalArgumentException");
            // 两侧均仅有 2 个公有构造（无 (String,Throwable) 形态，javap 实证）
            check(NumberFormatException.class.getDeclaredConstructors().length == 2,
                  "exactly 2 declared constructors (no (String,Throwable) variant)");
        } catch (SecurityException se) {
            throw new AssertionError(se);
        }
    }

    static void testThrowableContract() {
        // 构造链与消息透传
        NumberFormatException e1 = new NumberFormatException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        check(e1.getCause() == null, "no-arg ctor null cause");
        check(e1.toString().equals("java.lang.NumberFormatException"),
              "toString no-arg exact");
        NumberFormatException e2 = new NumberFormatException("bad");
        check("bad".equals(e2.getMessage()), "(String) ctor message");
        check(e2.getCause() == null, "(String) ctor null cause");
        check(e2.toString().equals("java.lang.NumberFormatException: bad"),
              "toString with message exact");
        check(e2.getLocalizedMessage().equals("bad"), "getLocalizedMessage == getMessage");
        // 消息透传：unicode 与长文本
        NumberFormatException u = new NumberFormatException("数值越界");
        check("数值越界".equals(u.getMessage()), "unicode message transparency");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append("x");
        NumberFormatException longMsg = new NumberFormatException(sb.toString());
        check(longMsg.getMessage().length() == 100, "long message transparent");
        // initCause 契约：一次成功、二次 IllegalStateException
        NumberFormatException t = new NumberFormatException("wrap");
        Exception cause = new IllegalStateException("root");
        check(t.initCause(cause) == t, "initCause returns this");
        check(t.getCause() == cause, "getCause identity");
        check("root".equals(t.getCause().getMessage()), "cause message chained");
        try {
            t.initCause(new Exception("second"));
            check(false, "initCause twice should throw IllegalStateException");
        } catch (IllegalStateException expected) {
            check(true, "initCause twice throws IllegalStateException");
        }
        // (String) 构造后 initCause 仍可补设（cause 未初始化时）
        NumberFormatException t2 = new NumberFormatException("m");
        Exception cause2 = new NumberFormatException("inner");
        t2.initCause(cause2);
        check(t2.getCause() == cause2, "initCause after (String) ctor");
        // 栈轨迹与 suppressed
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        Throwable sup = new NumberFormatException("sup");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup,
              "addSuppressed/getSuppressed identity");
        // fillInStackTrace 返回自身
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
    }

    static void testThrownFamily() {
        // 各包装 parse 家族的 NFE 与精确消息（实测逐字锁定；null 消息两侧不同）
        checkNfeMsg("For input string: \"not-a-number\"",
              () -> Integer.parseInt("not-a-number"), "Integer.parseInt alpha");
        checkNfeMsg("For input string: \"z\"", () -> Long.parseLong("z"), "Long.parseLong alpha");
        checkNfeMsg("For input string: \"z\"", () -> Short.parseShort("z"), "Short.parseShort alpha");
        checkNfeMsg("For input string: \"z\"", () -> Byte.parseByte("z"), "Byte.parseByte alpha");
        checkNfeMsg(MODERN ? "Cannot parse null string" : "null",
              () -> Integer.parseInt(null), "Integer.parseInt(null) message");
        checkNfeMsg(MODERN ? "Cannot parse null string" : "null",
              () -> Long.parseLong(null), "Long.parseLong(null) message");
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Integer.parseInt("10", 1), "Integer.parseInt radix 1");
        checkNfeMsg("Value out of range. Value:\"32768\" Radix:10",
              () -> Short.parseShort("32768"), "Short.parseShort overflow message");
        checkNfeMsg("Value out of range. Value:\"128\" Radix:10",
              () -> Byte.parseByte("128"), "Byte.parseByte overflow message");
        checkNfeMsg("Zero length string", () -> Integer.decode(""), "Integer.decode empty message");
        checkNfeMsg("Illegal leading minus sign on unsigned string -1.",
              () -> Integer.parseUnsignedInt("-1"), "parseUnsignedInt minus message");
        checkNfeMsg("String value 4294967296 exceeds range of unsigned int.",
              () -> Integer.parseUnsignedInt("4294967296"), "parseUnsignedInt overflow message");
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
            try {
                r.run();
                check(false, labels[i] + ": expected NumberFormatException");
            } catch (NumberFormatException expected) {
                check(true, labels[i] + " throws NumberFormatException");
            }
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        // 契约深化（2026-11）
        testHierarchyFull();
        testThrowableContract();
        testThrownFamily();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
