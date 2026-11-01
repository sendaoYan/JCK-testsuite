/*
 * @test
 * @summary java.util.logging.Filter conformance: isLoggable predicate.
 * @run main FilterTest
 */

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFilter() {
        Filter f = new Filter() {
            public boolean isLoggable(LogRecord record) {
                return record.getMessage().contains("log");
            }
        };
        check(f.isLoggable(new LogRecord(Level.INFO, "log this")), "isLoggable true");
        check(!f.isLoggable(new LogRecord(Level.INFO, "skip")), "isLoggable false");
    }

    public static void main(String[] args) {
        testFilter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
