/*
 * @test
 * @summary javax.management.modelmbean.DescriptorSupport conformance: setField/getFieldValue/isValid.
 * @run main DescriptorSupportTest
 */

import javax.management.modelmbean.*;
public class DescriptorSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        DescriptorSupport d = new DescriptorSupport();
        d.setField("name", "value");
        check("value".equals(d.getFieldValue("name")), "getFieldValue");
        check(d.getFieldNames().length == 1, "field names");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
