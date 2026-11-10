/*
 * @test
 * @summary java.util.logging.Formatter conformance: white-box structure + black-box behavior.
 * @run main FormatterTest
 */

import java.lang.reflect.Modifier;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.Formatter");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected constructor");
        check(Modifier.isAbstract(c.getMethod("format", LogRecord.class).getModifiers()), "format is abstract");
        check(hasMethod(c, "getHead", Handler.class), "getHead(Handler)");
        check(hasMethod(c, "getTail", Handler.class), "getTail(Handler)");
        check(hasMethod(c, "formatMessage", LogRecord.class), "formatMessage(LogRecord)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Formatter f = new Formatter() {
            public String format(LogRecord record) {
                return "[" + record.getMessage() + "]";
            }
        };
        check("[msg]".equals(f.format(new LogRecord(Level.INFO, "msg"))), "custom format");
        check("".equals(f.getHead(null)), "getHead default empty");
        check("".equals(f.getTail(null)), "getTail default empty");

        // formatMessage: no parameters -> raw message
        check("hello".equals(f.formatMessage(new LogRecord(Level.INFO, "hello"))),
                "formatMessage without params");

        // formatMessage: java.text style {0}/{1} substitution
        LogRecord r = new LogRecord(Level.INFO, "a={0} b={1}");
        r.setParameters(new Object[] {Integer.valueOf(1), Integer.valueOf(2)});
        check("a=1 b=2".equals(f.formatMessage(r)), "formatMessage {0}/{1} substitution");

        // formatMessage: resource bundle key lookup + substitution
        ResourceBundle rb = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][] {{"greeting", "Hello {0}!"}};
            }
        };
        LogRecord localized = new LogRecord(Level.INFO, "greeting");
        localized.setResourceBundle(rb);
        localized.setResourceBundleName("testBundle");
        localized.setParameters(new Object[] {"World"});
        check("Hello World!".equals(f.formatMessage(localized)),
                "formatMessage resource-bundle key resolution");

        // formatMessage: null message safe
        LogRecord nullMsg = new LogRecord(Level.INFO, null);
        check(f.formatMessage(nullMsg) == null, "formatMessage null message -> null");
    }


    // ===================== 契约深化：抽象 format 契约/getHead-getTail 默认空串/格式化 message 参数拼接 =====================
    static void testContracts() {
        java.util.logging.Formatter f = new java.util.logging.Formatter() {
            public String format(java.util.logging.LogRecord r) {
                return r.getLevel() + ":" + r.getMessage();
            }
        };
        java.util.logging.LogRecord r = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "fm");
        check("INFO:fm".equals(f.format(r)), "formatMessage-free custom format works");
        check("".equals(f.getHead(null)) && "".equals(f.getTail(null)), "default head/tail empty");
        // formatMessage：MessageFormat 插值（无 bundle）
        r.setParameters(new Object[] {"A", "B"});
        java.util.logging.LogRecord pf = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "{0}-{1}");
        pf.setParameters(new Object[] {"A", "B"});
        check("A-B".equals(f.formatMessage(pf)), "formatMessage interpolates parameters");
        // 无参数时原样返回
        check("plain".equals(f.formatMessage(new java.util.logging.LogRecord(java.util.logging.Level.INFO, "plain"))),
              "formatMessage without params unchanged");
        // 无参数但含 {0} 占位符：不替换，原样返回（不抛异常）
        check("a={0}".equals(f.formatMessage(new java.util.logging.LogRecord(java.util.logging.Level.INFO, "a={0}"))),
              "formatMessage null params leaves braces literal");
        // bundle 缺 key：回退到原始 message
        java.util.logging.LogRecord miss = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "nokey");
        miss.setResourceBundle(new ListResourceBundle() {
            protected Object[][] getContents() { return new Object[][] {{"k", "v"}}; }
        });
        check("nokey".equals(f.formatMessage(miss)), "formatMessage falls back when bundle key missing");
        // getHead/getTail 对非 null handler 亦为空串
        Handler dummy = new Handler() {
            public void publish(java.util.logging.LogRecord r) {}
            public void flush() {}
            public void close() {}
        };
        check("".equals(f.getHead(dummy)) && "".equals(f.getTail(dummy)),
              "getHead/getTail empty for non-null handler");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
