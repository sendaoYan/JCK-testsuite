/*
 * @test
 * @summary java.util.logging.SimpleFormatter conformance: format output contains level/message.
 * @run main SimpleFormatterTest
 */

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class SimpleFormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSimpleFormatter() {
        SimpleFormatter f = new SimpleFormatter();
        String out = f.format(new LogRecord(Level.INFO, "hello"));
        check(out.contains("hello"), "contains message");
        check(out.contains("INFO"), "contains level");
        check("".equals(f.getHead(null)), "getHead empty");
        check("".equals(f.getTail(null)), "getTail empty");
    }

    public static void main(String[] args) {
        testSimpleFormatter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
