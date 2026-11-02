/*
 * @test
 * @summary javax.management.MBeanAttributeInfo conformance: type/readable/writable/is.
 * @run main MBeanAttributeInfoTest
 */

import javax.management.*;
public class MBeanAttributeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanAttributeInfo a = new MBeanAttributeInfo("Count", "int", "count", true, true, false);
        check("Count".equals(a.getName()), "name");
        check("int".equals(a.getType()), "type");
        check(a.isReadable() && a.isWritable() && !a.isIs(), "flags");
        MBeanAttributeInfo c = (MBeanAttributeInfo) a.clone();
        check(a.equals(c), "clone equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
