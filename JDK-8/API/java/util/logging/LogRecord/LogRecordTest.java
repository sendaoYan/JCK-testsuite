/*
 * @test
 * @summary java.util.logging.LogRecord conformance: level/message/sequence/parameters/accessors.
 * @run main LogRecordTest
 */

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogRecordTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLogRecord() {
        LogRecord r = new LogRecord(Level.INFO, "message");
        check(r.getLevel() == Level.INFO, "getLevel");
        check("message".equals(r.getMessage()), "getMessage");
        r.setLevel(Level.WARNING);
        check(r.getLevel() == Level.WARNING, "setLevel");
        r.setMessage("new");
        check("new".equals(r.getMessage()), "setMessage");
        r.setLoggerName("jck.logger");
        check("jck.logger".equals(r.getLoggerName()), "set/getLoggerName");
        r.setMillis(123456L);
        check(r.getMillis() == 123456L, "set/getMillis");
        check(r.getSequenceNumber() >= 0, "getSequenceNumber");
        r.setThreadID(42);
        check(r.getThreadID() == 42, "set/getThreadID");
        r.setSourceClassName("cls");
        check("cls".equals(r.getSourceClassName()), "set/getSourceClassName");
        r.setSourceMethodName("mth");
        check("mth".equals(r.getSourceMethodName()), "set/getSourceMethodName");
        Object[] params = {Integer.valueOf(1), Integer.valueOf(2)};
        r.setParameters(params);
        check(r.getParameters() == params, "set/getParameters");
        r.setThrown(new RuntimeException("x"));
        check(r.getThrown() != null, "set/getThrown");
        check(r.getResourceBundle() == null, "getResourceBundle null");
        check(r.getResourceBundleName() == null, "getResourceBundleName null");
        check(r.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testLogRecord();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
