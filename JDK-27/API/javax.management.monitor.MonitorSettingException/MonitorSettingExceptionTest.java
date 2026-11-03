/*
 * @test
 * @summary javax.management.monitor.MonitorSettingException conformance: white-box structure + black-box behavior.
 * @run main MonitorSettingExceptionTest
 */

import javax.management.JMRuntimeException;
import javax.management.monitor.MonitorSettingException;

public class MonitorSettingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.MonitorSettingException");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(JMRuntimeException.class.isAssignableFrom(c), "extends JMRuntimeException");
        check(c.getSuperclass() == JMRuntimeException.class, "direct superclass is JMRuntimeException");
        check(c.getConstructors().length == 2, "no-arg and String constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MonitorSettingException e1 = new MonitorSettingException("msg");
        check("msg".equals(e1.getMessage()), "String message");
        check(e1 instanceof JMRuntimeException, "instance of JMRuntimeException");
        check(e1 instanceof RuntimeException, "instance of RuntimeException");

        MonitorSettingException e2 = new MonitorSettingException();
        check(e2.getMessage() == null, "no-arg null message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
