/*
 * @test
 * @summary javax.management.AttributeValueExp conformance: attribute name access.
 * @run main AttributeValueExpTest
 */

import javax.management.*;
public class AttributeValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        AttributeValueExp e = new AttributeValueExp("Count");
        check("Count".equals(e.getAttributeName()), "getAttributeName");
        check(new AttributeValueExp().getAttributeName() == null, "no-arg null");
        e.setMBeanServer(null);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
