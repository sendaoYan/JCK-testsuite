/*
 * @test
 * @summary javax.management.AttributeChangeNotificationFilter conformance: enableAttribute filtering.
 * @run main AttributeChangeNotificationFilterTest
 */

import javax.management.*;
public class AttributeChangeNotificationFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("Count");
        AttributeChangeNotification a = new AttributeChangeNotification("src", 1L, 2L, "jmx.attribute.change", "Count", "int", "old", "new");
        AttributeChangeNotification b = new AttributeChangeNotification("src", 1L, 2L, "jmx.attribute.change", "Other", "int", "old", "new");
        check(f.isNotificationEnabled(a), "enabled attribute");
        check(!f.isNotificationEnabled(b), "disabled attribute");
        check(f.getEnabledAttributes().contains("Count"), "getEnabledAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
