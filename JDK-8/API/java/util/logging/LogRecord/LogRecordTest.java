/*
 * @test
 * @summary java.util.logging.LogRecord conformance: white-box structure + black-box behavior.
 * @run main LogRecordTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogRecordTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.LogRecord");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor(Level.class, String.class) != null, "(Level,String) constructor");
        String[] methods = {"getLoggerName","setLoggerName","getResourceBundle","setResourceBundle",
                "getResourceBundleName","setResourceBundleName","getLevel","setLevel",
                "getSequenceNumber","setSequenceNumber","getSourceClassName","setSourceClassName",
                "getSourceMethodName","setSourceMethodName","getMessage","setMessage",
                "getParameters","setParameters","getThreadID","setThreadID",
                "getMillis","setMillis","getThrown","setThrown"};
        for (String m : methods) {
            check(hasMethod(c, m), "has method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LogRecord r = new LogRecord(Level.INFO, "message");
        check(r.getLevel() == Level.INFO, "constructor level");
        check("message".equals(r.getMessage()), "constructor message");

        // level / message round-trip
        r.setLevel(Level.WARNING);
        check(r.getLevel() == Level.WARNING, "set/getLevel");
        r.setMessage("new");
        check("new".equals(r.getMessage()), "set/getMessage");

        // logger name
        r.setLoggerName("jck.logger");
        check("jck.logger".equals(r.getLoggerName()), "set/getLoggerName");

        // fixed epoch millis round-trip
        r.setMillis(123456789L);
        check(r.getMillis() == 123456789L, "set/getMillis");

        // sequence number: auto-incrementing across records
        LogRecord r1 = new LogRecord(Level.INFO, "a");
        LogRecord r2 = new LogRecord(Level.INFO, "b");
        check(r1.getSequenceNumber() >= 0, "sequence non-negative");
        check(r2.getSequenceNumber() > r1.getSequenceNumber(), "sequence increments");
        r.setSequenceNumber(4242L);
        check(r.getSequenceNumber() == 4242L, "set/getSequenceNumber");

        // thread id
        r.setThreadID(42);
        check(r.getThreadID() == 42, "set/getThreadID");

        // source class/method
        r.setSourceClassName("cls");
        check("cls".equals(r.getSourceClassName()), "set/getSourceClassName");
        r.setSourceMethodName("mth");
        check("mth".equals(r.getSourceMethodName()), "set/getSourceMethodName");

        // parameters
        Object[] params = {Integer.valueOf(1), Integer.valueOf(2)};
        r.setParameters(params);
        check(r.getParameters() == params, "set/getParameters");

        // thrown
        RuntimeException thrown = new RuntimeException("x");
        r.setThrown(thrown);
        check(r.getThrown() == thrown, "set/getThrown");

        // resource bundle + name
        ResourceBundle rb = new ListResourceBundle() {
            protected Object[][] getContents() { return new Object[][] {{"k", "v"}}; }
        };
        r.setResourceBundle(rb);
        check(r.getResourceBundle() == rb, "set/getResourceBundle");
        r.setResourceBundleName("jck.bundle");
        check("jck.bundle".equals(r.getResourceBundleName()), "set/getResourceBundleName");

        check(r.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
