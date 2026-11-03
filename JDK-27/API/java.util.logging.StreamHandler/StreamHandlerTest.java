/*
 * @test
 * @summary java.util.logging.StreamHandler conformance: white-box structure + black-box behavior.
 * @run main StreamHandlerTest
 */

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.logging.Formatter;
import java.util.logging.Handler;
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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName() +
                    " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but none thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.StreamHandler");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Handler.class.isAssignableFrom(c), "extends Handler");
        check(c.getSuperclass() == Handler.class, "direct superclass Handler");
        check(c.getConstructor(java.io.OutputStream.class, Formatter.class) != null,
                "(OutputStream,Formatter) constructor");
        check(c.getConstructor() != null, "no-arg constructor");
        // setOutputStream is protected
        check(Modifier.isProtected(c.getDeclaredMethod("setOutputStream", java.io.OutputStream.class).getModifiers()),
                "setOutputStream is protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // custom formatter exposes getHead/getTail to prove stream lifecycle
        Formatter fixed = new Formatter() {
            public String format(LogRecord record) { return "[" + record.getMessage() + "]\n"; }
            public String getHead(Handler h) { return "HEAD\n"; }
            public String getTail(Handler h) { return "TAIL\n"; }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamHandler sh = new StreamHandler(out, fixed);
        sh.setEncoding("UTF-8");
        check(sh.getLevel() == Level.INFO, "default level INFO");
        check(sh.getEncoding().equals("UTF-8"), "setEncoding UTF-8");

        check(sh.isLoggable(new LogRecord(Level.INFO, "m")), "isLoggable INFO true");
        check(!sh.isLoggable(new LogRecord(Level.FINE, "m")), "isLoggable FINE false");

        sh.publish(new LogRecord(Level.INFO, "m1"));
        sh.flush();
        check(out.toString("UTF-8").contains("HEAD\n[m1]\n"), "publish writes head then message");

        sh.close();
        check("HEAD\n[m1]\nTAIL\n".equals(out.toString("UTF-8")),
                "full stream output = head + message + tail");

        // level filtering: below-level records are not written
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        StreamHandler sh2 = new StreamHandler(out2, new SimpleFormatter());
        sh2.setEncoding("UTF-8");
        sh2.setLevel(Level.WARNING);
        sh2.publish(new LogRecord(Level.INFO, "dropped"));
        sh2.publish(new LogRecord(Level.SEVERE, "kept"));
        sh2.flush();
        String text2 = out2.toString("UTF-8");
        check(!text2.contains("dropped"), "below-level record not written");
        check(text2.contains("kept"), "at-level record written");

        // constructor rejects null formatter
        expectThrows(NullPointerException.class,
                () -> new StreamHandler(new ByteArrayOutputStream(), null),
                "StreamHandler(out, null) NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
