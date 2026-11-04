/*
 * @test
 * @summary javax.management.JMX conformance: isMXBeanInterface and field constants.
 * @run main JMXTest
 */

import javax.management.*;

public class JMXTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public interface FooMXBean {}

    public static void main(String[] args) throws Exception {
        check(JMX.isMXBeanInterface(FooMXBean.class), "MXBean interface");
        check(!JMX.isMXBeanInterface(java.lang.Object.class), "non-MXBean");
        check(JMX.DEFAULT_VALUE_FIELD != null && JMX.IMMUTABLE_INFO_FIELD != null, "field constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
