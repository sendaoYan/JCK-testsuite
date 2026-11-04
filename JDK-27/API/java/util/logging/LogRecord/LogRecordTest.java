/*
 * @test
 * @summary java.util.logging.LogRecord conformance: white-box structure + black-box behavior.
 * @run main LogRecordTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogRecordTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.LogRecord");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor(Level.class, String.class) != null, "(Level,String) constructor");
        String[] methods = {"getLoggerName","setLoggerName","getResourceBundle","setResourceBundle",
                "getResourceBundleName","setResourceBundleName","getLevel","setLevel",
                "getSequenceNumber","setSequenceNumber","getSourceClassName","setSourceClassName",
                "getSourceMethodName","setSourceMethodName","getMessage","setMessage",
                "getParameters","setParameters","getThreadID","setThreadID","getLongThreadID",
                "setLongThreadID","getMillis","setMillis","getInstant","setInstant",
                "getThrown","setThrown"};
        for (String m : methods) {
            check(hasMethod(c, m), "has method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LogRecord r = new LogRecord(Level.INFO, "message");
        check(r.getLevel() == Level.INFO, "constructor level");
        check("message".equals(r.getMessage()), "constructor message");

        // level / message round-trip
        r.setLevel(Level.WARNING);
        check(r.getLevel() == Level.WARNING, "set/getLevel");
        r.setMessage("new");
        check("new".equals(r.getMessage()), "set/getMessage");

        // logger name
        r.setLoggerName("jck.logger");
        check("jck.logger".equals(r.getLoggerName()), "set/getLoggerName");

        // fixed epoch millis + instant round-trip
        r.setMillis(123456789L);
        check(r.getMillis() == 123456789L, "set/getMillis");
        check(r.getInstant().equals(Instant.ofEpochMilli(123456789L)), "getInstant from millis");
        Instant fixed = Instant.ofEpochMilli(987654321L);
        r.setInstant(fixed);
        check(fixed.equals(r.getInstant()), "set/getInstant");
        check(r.getMillis() == 987654321L, "getMillis derives from instant");

        // sequence number: auto-incrementing across records
        LogRecord r1 = new LogRecord(Level.INFO, "a");
        LogRecord r2 = new LogRecord(Level.INFO, "b");
        check(r1.getSequenceNumber() >= 0, "sequence non-negative");
        check(r2.getSequenceNumber() > r1.getSequenceNumber(), "sequence increments");
        r.setSequenceNumber(4242L);
        check(r.getSequenceNumber() == 4242L, "set/getSequenceNumber");

        // thread id
        r.setThreadID(42);
        check(r.getThreadID() == 42, "set/getThreadID");
        check(r.getLongThreadID() == 42L, "setThreadID also sets longThreadID");
        check(r.setLongThreadID(1234567890123L) == r, "setLongThreadID returns this");
        check(r.getLongThreadID() == 1234567890123L, "getLongThreadID round-trip");

        // source class/method
        r.setSourceClassName("cls");
        check("cls".equals(r.getSourceClassName()), "set/getSourceClassName");
        r.setSourceMethodName("mth");
        check("mth".equals(r.getSourceMethodName()), "set/getSourceMethodName");

        // parameters
        Object[] params = {Integer.valueOf(1), Integer.valueOf(2)};
        r.setParameters(params);
        check(r.getParameters() == params, "set/getParameters");

        // thrown
        RuntimeException thrown = new RuntimeException("x");
        r.setThrown(thrown);
        check(r.getThrown() == thrown, "set/getThrown");

        // resource bundle + name
        ResourceBundle rb = new ListResourceBundle() {
            protected Object[][] getContents() { return new Object[][] {{"k", "v"}}; }
        };
        r.setResourceBundle(rb);
        check(r.getResourceBundle() == rb, "set/getResourceBundle");
        r.setResourceBundleName("jck.bundle");
        check("jck.bundle".equals(r.getResourceBundleName()), "set/getResourceBundleName");

        check(r.toString() != null, "toString non-null");
    }


    // ===================== 契约深化：字段全往返/序列号单调/null 契约 =====================
    static void testContracts() {
        java.util.logging.LogRecord r = new java.util.logging.LogRecord(java.util.logging.Level.WARNING, "msg-{0}");
        // 级别与消息
        check(r.getLevel() == java.util.logging.Level.WARNING && "msg-{0}".equals(r.getMessage()),
              "level and message round-trip");
        // 参数/异常
        Object[] params = {"a", 42};
        r.setParameters(params);
        check(java.util.Arrays.equals(r.getParameters(), params), "parameters round-trip");
        RuntimeException boom = new RuntimeException("boom");
        r.setThrown(boom);
        check(r.getThrown() == boom, "thrown identity");
        check(r.getThrown() instanceof RuntimeException && "boom".equals(r.getThrown().getMessage()),
              "thrown preserved");
        // logger 名 / 源类名 / 方法名
        r.setLoggerName("jck.lr");
        check("jck.lr".equals(r.getLoggerName()), "loggerName set/get");
        r.setSourceClassName("C");
        r.setSourceMethodName("m");
        check("C".equals(r.getSourceClassName()) && "m".equals(r.getSourceMethodName()),
              "source class/method round-trip");
        check(new java.util.logging.LogRecord(java.util.logging.Level.INFO, "x").getSourceClassName() == null,
              "source class defaults null (lazy inference off stack)");
        // 序列号：全局单调递增
        java.util.logging.LogRecord r1 = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "1");
        java.util.logging.LogRecord r2 = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "2");
        check(r2.getSequenceNumber() > r1.getSequenceNumber(), "sequence numbers strictly increase");
        r.setSequenceNumber(12345L);
        check(r.getSequenceNumber() == 12345L, "sequenceNumber settable");
        // threadID / millis
        r.setThreadID(77);
        check(r.getThreadID() == 77, "threadID set/get");
        r.setMillis(0L);
        check(r.getMillis() == 0L, "millis set/get (epoch allowed)");
        check(new java.util.logging.LogRecord(java.util.logging.Level.INFO, "x").getMillis() > 0,
              "default millis is current time");
        // resourceBundleName
        r.setResourceBundleName("jck.NoBundle");
        check("jck.NoBundle".equals(r.getResourceBundleName()), "resourceBundleName set/get");
        check(r.getResourceBundle() == null, "bundle object stays null when only name set");
        // null 级别构造 NPE
        try {
            new java.util.logging.LogRecord(null, "x");
            check(false, "null level ctor NPE");
        } catch (NullPointerException e) { check(true, "null level ctor NPE"); }
        // null 消息允许（格式化端处理）
        java.util.logging.LogRecord n = new java.util.logging.LogRecord(java.util.logging.Level.INFO, null);
        check(n.getMessage() == null, "null message allowed");
    }


    // ===================== JDK-9+ 增量：getInstant/setInstant/getLongThreadID（JDK-27 侧锁定） =====================
    static void testJdk27Additions() {
        java.util.logging.LogRecord r = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "m");
        java.time.Instant now = r.getInstant();
        check(now != null, "getInstant non-null");
        check(r.getMillis() == now.toEpochMilli(), "getMillis consistent with getInstant");
        check(r.getLongThreadID() == (long) r.getThreadID(), "long threadID consistent with int threadID");
        r.setInstant(java.time.Instant.EPOCH);
        check(r.getInstant().equals(java.time.Instant.EPOCH) && r.getMillis() == 0L,
              "setInstant(EPOCH) reflects into millis");
        // nano 精度保留
        java.time.Instant precise = java.time.Instant.ofEpochSecond(5, 123456789);
        r.setInstant(precise);
        check(r.getInstant().getNano() == 123456789, "nanosecond precision preserved");
    }

    public static void main(String[] args) throws Exception {
        try { testJdk27Additions(); } catch (Exception e) { throw new RuntimeException(e); }
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
