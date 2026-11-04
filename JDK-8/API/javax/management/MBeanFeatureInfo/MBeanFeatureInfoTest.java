/*
 * @test
 * @summary javax.management.MBeanFeatureInfo conformance: name/description/descriptor.
 * @run main MBeanFeatureInfoTest
 */

import javax.management.*;
public class MBeanFeatureInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanFeatureInfo f = new MBeanFeatureInfo("name", "desc");
        check("name".equals(f.getName()), "name");
        check("desc".equals(f.getDescription()), "description");
        MBeanFeatureInfo g = new MBeanFeatureInfo("n", "d", new ImmutableDescriptor(new String[]{"a"}, new Object[]{"b"}));
        check(g.getDescriptor() != null && "b".equals(g.getDescriptor().getFieldValue("a")), "descriptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
