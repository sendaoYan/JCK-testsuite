/*
 * @test
 * @summary javax.management.StringValueExp conformance: value access.
 * @run main StringValueExpTest
 */

import javax.management.*;
public class StringValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        StringValueExp e = new StringValueExp("hello");
        check("hello".equals(e.getValue()), "getValue");
        check(e.toString() != null, "toString");
        check(new StringValueExp().getValue() == null, "no-arg null value");
        e.setMBeanServer(null);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
