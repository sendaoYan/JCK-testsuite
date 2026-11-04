/*
 * @test
 * @summary javax.management.AttributeChangeNotification conformance: attribute name/type/old/new.
 * @run main AttributeChangeNotificationTest
 */

import javax.management.*;
public class AttributeChangeNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Object oldV = "old", newV = "new";
        AttributeChangeNotification n = new AttributeChangeNotification("src", 1L, 2L, "jmx.attribute.change", "Count", "int", oldV, newV);
        check("Count".equals(n.getAttributeName()), "attribute name");
        check("int".equals(n.getAttributeType()), "attribute type");
        check(oldV.equals(n.getOldValue()), "old value");
        check(newV.equals(n.getNewValue()), "new value");
        check("jmx.attribute.change".equals(n.getType()), "type");
        check(AttributeChangeNotification.ATTRIBUTE_CHANGE.equals("jmx.attribute.change"), "ATTRIBUTE_CHANGE constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
