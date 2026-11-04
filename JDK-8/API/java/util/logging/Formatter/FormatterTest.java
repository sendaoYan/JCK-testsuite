/*
 * @test
 * @summary java.util.logging.Formatter conformance: format/formatMessage/getHead/getTail.
 * @run main FormatterTest
 */

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormatter() {
        Formatter f = new Formatter() {
            public String format(LogRecord record) {
                return record.getMessage();
            }
        };
        check("msg".equals(f.format(new LogRecord(Level.INFO, "msg"))), "format");
        check("hello".equals(f.formatMessage(new LogRecord(Level.INFO, "hello"))), "formatMessage");
        check("".equals(f.getHead(null)), "getHead default empty");
        check("".equals(f.getTail(null)), "getTail default empty");
    }

    public static void main(String[] args) {
        testFormatter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
