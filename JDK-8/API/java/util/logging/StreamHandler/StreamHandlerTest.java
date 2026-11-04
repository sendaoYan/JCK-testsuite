/*
 * @test
 * @summary java.util.logging.StreamHandler conformance: writes formatted records to a stream.
 * @run main StreamHandlerTest
 */

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class StreamHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStreamHandler() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamHandler sh = new StreamHandler(out, new SimpleFormatter());
        sh.publish(new LogRecord(Level.INFO, "hello stream"));
        sh.flush();
        check(out.size() > 0, "output written");
        check(out.toString().contains("hello stream"), "message written");
        sh.close();
    }

    public static void main(String[] args) {
        testStreamHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
