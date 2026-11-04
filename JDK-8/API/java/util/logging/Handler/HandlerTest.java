/*
 * @test
 * @summary java.util.logging.Handler conformance: level/formatter/filter/isLoggable.
 * @run main HandlerTest
 */

import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class HandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHandler() {
        Handler h = new Handler() {
            public void publish(LogRecord record) {
            }

            public void flush() {
            }

            public void close() {
            }
        };
        h.setLevel(Level.WARNING);
        check(h.getLevel() == Level.WARNING, "set/getLevel");
        h.setFormatter(new SimpleFormatter());
        check(h.getFormatter() instanceof SimpleFormatter, "getFormatter");
        Filter f = new Filter() {
            public boolean isLoggable(LogRecord record) {
                return true;
            }
        };
        h.setFilter(f);
        check(h.getFilter() == f, "getFilter");
        h.setErrorManager(new ErrorManager());
        check(h.getErrorManager() != null, "getErrorManager");
        check(h.isLoggable(new LogRecord(Level.SEVERE, "x")), "isLoggable SEVERE");
        check(!h.isLoggable(new LogRecord(Level.INFO, "x")), "isLoggable INFO below level");
    }

    public static void main(String[] args) {
        testHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
