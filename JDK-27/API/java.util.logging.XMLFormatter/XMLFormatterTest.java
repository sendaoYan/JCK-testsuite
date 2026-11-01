/*
 * @test
 * @summary java.util.logging.XMLFormatter conformance: XML record output and head/tail.
 * @run main XMLFormatterTest
 */

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

public class XMLFormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testXMLFormatter() {
        XMLFormatter f = new XMLFormatter();
        String out = f.format(new LogRecord(Level.INFO, "hello"));
        check(out.contains("<record>"), "contains record element");
        check(out.contains("hello"), "contains message");
        check(out.contains("<level>INFO</level>"), "contains level");
        check(f.getHead(null) != null, "getHead non-null");
        check(f.getTail(null) != null, "getTail non-null");
    }

    public static void main(String[] args) {
        testXMLFormatter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
