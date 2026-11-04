/*
 * @test
 * @summary java.util.logging.XMLFormatter conformance: white-box structure + black-box behavior.
 * @run main XMLFormatterTest
 */

import java.lang.reflect.Modifier;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

public class XMLFormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.XMLFormatter");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Formatter.class.isAssignableFrom(c), "extends Formatter");
        check(c.getSuperclass() == Formatter.class, "direct superclass Formatter");
        check(c.getConstructor() != null, "no-arg constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        XMLFormatter f = new XMLFormatter();

        LogRecord r = new LogRecord(Level.INFO, "hello");
        r.setLoggerName("jck.xml");
        r.setMillis(1700000000000L);
        String out = f.format(r);
        check(out.startsWith("<record>"), "format starts with <record>");
        check(out.contains("<level>INFO</level>"), "format contains level element");
        check(out.contains("<message>hello</message>"), "format contains message element");
        check(out.contains("<logger>jck.xml</logger>"), "format contains logger element");
        check(out.contains("<millis>"), "format contains millis element");
        check(out.contains("<sequence>"), "format contains sequence element");

        // level element reflects record level
        LogRecord warn = new LogRecord(Level.WARNING, "careful");
        warn.setMillis(1700000000000L);
        check(f.format(warn).contains("<level>WARNING</level>"), "level element reflects WARNING");

        // XML escaping of special characters
        LogRecord special = new LogRecord(Level.INFO, "a < b & c > d");
        special.setMillis(1700000000000L);
        String escaped = f.format(special);
        check(escaped.contains("&lt;") && escaped.contains("&amp;") && escaped.contains("&gt;"),
                "message XML-escaped");

        // getHead/getTail produce enclosing document tags
        String head = f.getHead(null);
        check(head.startsWith("<?xml"), "getHead starts with xml declaration");
        check(head.contains("<log>"), "getHead opens <log>");
        check(head.contains("<!DOCTYPE"), "getHead has DOCTYPE");
        check(f.getTail(null).contains("</log>"), "getTail closes </log>");
    }


    // ===================== 契约深化：record 结构/转义/head-tail 包裹/异常元素 =====================
    static void testContracts() {
        java.util.logging.XMLFormatter xf = new java.util.logging.XMLFormatter();
        java.util.logging.LogRecord r = new java.util.logging.LogRecord(java.util.logging.Level.WARNING, "a<b>&z");
        String s = xf.format(r);
        check(s.contains("<record>") && s.contains("</record>"), "record element");
        check(s.contains("<level>WARNING</level>"), "level element");
        check(s.contains("<message>a&lt;b&gt;&amp;z</message>"), "XML escaping of < > &");
        check(s.contains("<millis>"), "millis element present");
        check(s.contains("<sequence>"), "sequence element present");
        check(s.contains("<thread>"), "thread element present");
        // date 元素为 Instant 形式（带 Z 后缀）且新增 <nanos> 元素（JDK-9+ 形态锁定）
        check(s.contains("<date>") && s.contains("Z</date>"), "Instant UTC date form");
        check(s.contains("<nanos>"), "nanos element present (JDK-9+)");
        // getHead/getTail 包裹整个日志
        check(xf.getHead(null).contains("<log") && xf.getTail(null).contains("</log>"),
              "head/tail wrap <log> document");
        // 异常记录含 <exception> 与帧
        java.util.logging.LogRecord thr = new java.util.logging.LogRecord(java.util.logging.Level.SEVERE, "e");
        thr.setThrown(new RuntimeException("xml-boom"));
        String xs = xf.format(thr);
        check(xs.contains("<exception>") && xs.contains("xml-boom"), "exception element with message");
        check(xs.contains("<frame>"), "stack frame elements");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
