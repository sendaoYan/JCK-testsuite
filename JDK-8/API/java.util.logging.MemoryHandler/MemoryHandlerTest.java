/*
 * @test
 * @summary java.util.logging.MemoryHandler conformance: buffering and push level.
 * @run main MemoryHandlerTest
 */

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

public class MemoryHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryHandler() {
        Handler target = new ConsoleHandler();
        MemoryHandler mh = new MemoryHandler(target, 5, Level.SEVERE);
        check(mh.getPushLevel() == Level.SEVERE, "getPushLevel");
        mh.setPushLevel(Level.WARNING);
        check(mh.getPushLevel() == Level.WARNING, "setPushLevel");
        mh.setLevel(Level.ALL);
        check(mh.getLevel() == Level.ALL, "getLevel");
        mh.publish(new LogRecord(Level.INFO, "buffered"));
        mh.flush();
        mh.close();
    }

    public static void main(String[] args) {
        testMemoryHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
