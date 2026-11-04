/*
 * @test
 * @summary java.util.logging.ConsoleHandler conformance: default level/publish/flush/close.
 * @run main ConsoleHandlerTest
 */

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConsoleHandler() {
        ConsoleHandler h = new ConsoleHandler();
        check(h.getLevel() == Level.INFO, "default level INFO");
        h.publish(new LogRecord(Level.INFO, "console message"));
        h.flush();
        h.close();
    }

    public static void main(String[] args) {
        testConsoleHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
